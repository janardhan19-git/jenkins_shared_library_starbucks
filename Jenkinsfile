@Library('Jenkins_shared_library') _

pipeline {
    agent any

    parameters {
        choice(name: 'action', choices: ['create', 'delete'], description: 'Select create or destroy')
        string(name: 'DOCKER_HUB_USERNAME', defaultValue: 'janardhanmittapalli', description: 'Docker Hub Username')
        string(name: 'IMAGE_NAME', defaultValue: 'starbucks', description: 'Docker Image Name')
        string(name: 'K8S_MANIFEST_PATH', defaultValue: 'deployment.yaml', description: 'Path to your K8s deployment YAML')
    }

    tools {
        jdk 'jdk17'
        nodejs 'node16'
    }

    environment {
        BUILD_TAGGED_IMAGE = "${params.DOCKER_HUB_USERNAME}/${params.IMAGE_NAME}:${env.BUILD_NUMBER}"
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

        stage('NPM Install') {
            when { expression { params.action == 'create' } }
            steps {
                npmInstall()
            }
        }

        stage('Docker Build') {
            when { expression { params.action == 'create' } }
            steps {
                script {
                    dockerBuild(params.DOCKER_HUB_USERNAME, params.IMAGE_NAME, env.BUILD_NUMBER)
                }
            }
        }

        stage('Update K8s Manifest Image') {
            when { expression { params.action == 'create' } }
            steps {
                script {
                    def manifest = params.K8S_MANIFEST_PATH
                    def image = env.BUILD_TAGGED_IMAGE

                    // Replace image line using sed (assumes a line like: image: something)
                    sh """
                        sed -i 's|image: .*|image: ${image}|' ${manifest}
                        echo "✅ Updated ${manifest} with image: ${image}"
                    """
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
}
