def call() {
 sh "docker run -d --name starbucks -p 3000:3000 janardhanmittapalli/starbucks:latest"
}
