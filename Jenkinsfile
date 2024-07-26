pipeline {
    agent any

    environment {
        GIT_CREDENTIALS = '80fb7680-e9da-48aa-80b6-d96387fbafec' // ID de credenciales de Git en Jenkins
        DOCKER_HUB_CREDENTIALS = 'docker-hub-credentials' // ID de credenciales de Docker Hub en Jenkins
        DOCKER_IMAGE_TAG = "seiler18/mascachicles:AppFinalRelease-${env.BUILD_NUMBER}" // Tag de la imagen en Docker Hub
        SONARQUBE_SERVER = 'ProbandoSonar' // Nombre del servidor SonarQube configurado en Jenkins
        SONARQUBE_TOKEN = credentials('ProbandoSonar') // Token de autenticación para SonarQube
        NEXUS_CREDENTIALS = 'NexusLogin' // ID de credenciales de Nexus en Jenkins
        GROUP_ID = 'cl.talentodigital'
        ARTIFACT_ID = 'appmanageevents'
        VERSION = '0.0.1-RELEASE'
        PACKAGING = 'jar'
        FILE = 'target/appmanageevents-0.0.1-RELEASE.jar'
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/seiler18/AppManageEvents', branch: 'main', credentialsId: GIT_CREDENTIALS
            }
        }
        stage('Build and Package with Maven') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                script {
                    withSonarQubeEnv(SONARQUBE_SERVER) {
                        sh './mvnw sonar:sonar -Dsonar.token=${SONARQUBE_TOKEN}'
                    }
                }
            }
        }
        stage('Wait for Quality Gate') {
            steps {
                timeout(time: 1, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', DOCKER_HUB_CREDENTIALS) {
                        def dockerImage = docker.build("${DOCKER_IMAGE_TAG}", ".")
                        dockerImage.push()
                    }
                }
            }
        }
        stage('Publish to Nexus') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: NEXUS_CREDENTIALS, usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                        sh """
                        ./mvnw deploy -DskipTests
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed'
        }
        cleanup {
            echo 'Cleaning up old Docker images'
            sh '''
                docker images --filter "reference=seiler18/mascachicles" --format "{{.ID}}" | tail -n +3 | xargs -r docker rmi -f
                docker images --filter "reference=registry.hub.docker.com/seiler18/mascachicles" --format "{{.ID}}" | tail -n +3 | xargs -r docker rmi -f
            '''
        }
    }
}
