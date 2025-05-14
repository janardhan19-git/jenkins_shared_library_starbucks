def call() {
 withSonarQubeEnv('sonar-server') {
 sh '''$SCANNER_HOME/bin/sonar-scanner -Dsonar.projectName=starbucks \
 -Dsonar.projectKey=starbucks'''
 }
}