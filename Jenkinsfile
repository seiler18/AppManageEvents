pipeline {
    agent any

    environment {
        GIT_CREDENTIALS = credentials('80fb7680-e9da-48aa-80b6-d96387fbafec')
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/seiler18/AppManageEvents', credentialsId: "${env.GIT_CREDENTIALS}"
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
