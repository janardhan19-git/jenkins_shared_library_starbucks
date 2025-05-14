@Library('Jenkins_shared_library')

def COLOR_MAP = [
    'FAILURE' : 'danger',
    'SUCCESS' : 'good' 
]

pipeline
{
    agent any
    parameters
    {
        choice(name:'action',choices:'create\ndelete',description:'Select create or destroy')
        string(name: 'DOCKER_HUB_USERNAME', defaultValue: 'sammunde', description: 'Docker Hub Username')
        string(name: 'IMAGE_NAME', defaultValue: 'starbucks', description: 'Docker Image Name')


    }
    tools
    {
        jdk 'jdk17'
        nodejs 'node16'
    }
    environment 
    {
        SCANNER_HOME= tool 'sonar-scanner'
    }
    stages
    {
    stage('clean workspace')
        {
            steps
            {
                cleanWorkspace()
            }
        }
        stage('checkout from Git')
        {
            steps
            {
                checkoutGit('https://gitlab.com/s99070408/Starbucks-clone.git','main')
                // Link to Starbucks webpage git repo
            }
        }
        stage('sonarqube Analysis')
        {
            when {expression {params.action == 'create'}}
            steps{
                sonarqubeAnalysis()
            }
        }
        stage('sonarqube QualitGate')
        {
            when {expression {params.action == 'create'}}
            steps{
                script{
                    def credentialsId = 'Sonar-Admin-Token'
                    qualityGate(credentialsId)
                }
            }
        }
        stage('Npm')
        {
            when {expression{params.action == 'create'}}
            steps{
                npmInstall()
            }
        }
        stage('Trivy file scan')
        {
            when{expression {params.action == 'create'}}
            steps{
                trivyFs()
            }
        }
        stage('OWASP FS SCAN')
        {
            when{expression {params.action == 'create'}}
            steps {
                dependencyCheck additionalArguments: '--scan ./ --disableYarnAudit --disableNodeAudit', odcInstallation: 'DP-Check'
                dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
            }
        }
        stage('Docker Build')
        {
            when {expression{params.action == 'create'}}
            steps{
                script{
                    def dockerHubUsername = params.DOCKER_HUB_USERNAME
                    def imageName = params.IMAGE_NAME
                    dockerBuild(dockerHubUsername, imageName)
                }
            }
        }
        stage('Trivy image')
        {
            when {expression{params.action == 'create'}}
            steps {
                trivyImage()
            }
        }
        stage('Docker Scout Image') 
        {
            when {expression{params.action == 'create'}}
            steps {
                script{
                   withDockerRegistry(credentialsId: 'docker', toolName: 'docker'){
                       sh 'docker-scout quickview sammunde/starbucks:latest'
                       sh 'docker-scout cves sammunde/starbucks:latest'
                       sh 'docker-scout recommendations sammunde/starbucks:latest'
                   }
                }
            }
        }
        stage('Run container')
        {
            when {expression{params.action == 'create'}}
            steps
            {
                runContainer()
            }
        }
        stage('Remove Container')
        {
            when {expression{params.action == 'delete'}}
            steps{
                removeContainer()
            }
        }
    }
     post {
         always {
             echo 'Slack Notifications'
             slackSend (
                 channel: '#starbuck-clone',  
                 color: COLOR_MAP[currentBuild.currentResult],
                 message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} \n build ${env.BUILD_NUMBER} \n time More info at: ${env.BUILD_URL}"
               )
           }
       }
}
