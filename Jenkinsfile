pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/seiler18/AppManageEvents'
            }
        }
        stage('Build') {
            steps {
                sh './mvnw clean install'
            }
        }
        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }
        stage('Deploy') {
            steps {
                sh './mvnw spring-boot:run'
            }
        }
    }
}
