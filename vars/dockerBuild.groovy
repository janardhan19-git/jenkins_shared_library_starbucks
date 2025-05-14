def call(String dockerHubUsername, String imageName) {
 // Build the Docker image
 sh "docker build -t ${imageName} ." 
 // Tag the Docker image
 sh "docker tag ${imageName} ${dockerHubUsername}/${imageName}:latest"
 // Push the Docker image
 withDockerRegistry([url: 'https://index.docker.io/v1/', credentialsId: 'docker']) {
 sh "docker push ${dockerHubUsername}/${imageName}:latest"
 }
}