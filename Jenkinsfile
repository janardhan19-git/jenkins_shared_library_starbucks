@Library('Jenkins_shared_library') _

def COLOR_MAP = [
    'FAILURE' : 'danger',
    'SUCCESS' : 'good' 
]

pipeline {
    agent any

    parameters {
        choice(name: 'action', choices: ['create', 'delete'], description: 'Select create or destroy')
        string(name: 'DOCKER_HUB_USERNAME', defaultValue: 'janardhanmittapalli', description: 'Docker Hub Username')
        string(name: 'IMAGE_NAME', defaultValue: 'starbucks', description: 'Docker Image Name')
    }

    tools {
        jdk 'jdk17'
        nodejs 'node16'
    }

    environment {
        SCANNER_HOME = tool 'sonar-scanner'
    }

    stages {
        stage('Clean Workspace') {
            steps {
                cleanWorkspace()
            }
        }
        stage('Checkout from Git') {
            steps {
                checkoutGit('https://github.com/janardhan19-git/Starbucks-clone.git', 'main')
            }
        }
        stage('SonarQube Analysis') {
            when { expression { params.action == 'create' } }
            steps {
                sonarqubeAnalysis()
            }
        }
        stage('SonarQube QualityGate') {
            when { expression { params.action == 'create' } }
            steps {
                script {
                    def credentialsId = 'Sonar-token'
                    qualityGate(credentialsId)
                }
            }
        }
        stage('NPM Install') {
            when { expression { params.action == 'create' } }
            steps {
                npmInstall()
            }
        }
        stage('Trivy File Scan') {
            when { expression { params.action == 'create' } }
            steps {
                trivyFs()
            }
        }
        stage('OWASP FS Scan') {
            when { expression { params.action == 'create' } }
            steps {
                dependencyCheck additionalArguments: '--scan ./ --disableYarnAudit --disableNodeAudit', odcInstallation: 'DP-Check'
                dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
            }
        }
        stage('Docker Build') {
            when { expression { params.action == 'create' } }
            steps {
                script {
                    def dockerHubUsername = params.DOCKER_HUB_USERNAME
                    def imageName = params.IMAGE_NAME
                    dockerBuild(dockerHubUsername, imageName)
                }
            }
        }
        stage('Trivy Image Scan') {
            when { expression { params.action == 'create' } }
            steps {
                trivyImage()
            }
        }
        stage('Docker Scout Image') {
    when { expression { params.action == 'create' } }
    steps {
        script {
            def image = "${params.DOCKER_HUB_USERNAME}/${params.IMAGE_NAME}:latest"
            withDockerRegistry(credentialsId: 'docker', toolName: 'docker') {
                sh "docker-scout quickview ${image}"
                sh "docker-scout cves ${image}"
                sh "docker-scout recommendations ${image}"
            }
        }
    }
}

        stage('Run Container') {
            when { expression { params.action == 'create' } }
            steps {
                runContainer()
            }
        }
        stage('Remove Container') {
            when { expression { params.action == 'delete' } }
            steps {
                removeContainer()
            }
        }
    }

    post {
        always {
            emailext (
                attachLog: true,
                subject: "'${currentBuild.result}'",
                body: """Project: ${env.JOB_NAME}<br/>
                         Build Number: ${env.BUILD_NUMBER}<br/>
                         URL: ${env.BUILD_URL}<br/>""",
                to: 'janardhanmittapalli19@gmail.com',
                attachmentsPattern: 'trivyfs.txt,trivyimage.txt'
            )
        }
    }
}
