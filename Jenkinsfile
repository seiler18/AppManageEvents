pipeline {
    agent any

    environment {
        GIT_CREDENTIALS = '80fb7680-e9da-48aa-80b6-d96387fbafec' // ID de credenciales de Git en Jenkins
        DOCKER_HUB_CREDENTIALS = 'docker-hub-credentials' // ID de credenciales de Docker Hub en Jenkins
        DOCKER_IMAGE_TAG = "seiler18/mascachicles:jenkins-${env.BUILD_NUMBER}" // Despues de ":" va el tag de la imagen o quiere decir el nombre que tendra la imagen en el docker hub
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
        // Si deseas desplegar la aplicación, puedes habilitar esta etapa
        // stage('Deploy') {
        //     steps {
        //         sh './mvnw spring-boot:run'
        //     }
        // }
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
