pipeline {
    agent any

    environment {
        //Aca deberiamos definir las variables de entorno que se van a utilizar en el pipeline
        //Todas estas variables deberian apuntar a un perfil unico para la App , en este caso ocupe mis perfiles
        //GIT credentials , docker credentials, sonarqube server, nexus credentials, slack credentials
        //Estas credenciales seran ocupadas en el pipeline para poder hacer el build, push, deploy, etc , osea sus respectivas "steps" > "etapas"
        GIT_CREDENTIALS = '80fb7680-e9da-48aa-80b6-d96387fbafec'
        DOCKER_HUB_CREDENTIALS = 'docker-hub-credentials'
        DOCKER_IMAGE_TAG = "seiler18/mascachicles:AppFinalRelease-${env.BUILD_NUMBER}"
        SONARQUBE_SERVER = 'ProbandoSonar'
        SONARQUBE_TOKEN = credentials('ProbandoSonar')
        NEXUS_CREDENTIALS = 'NexusLogin'
        GROUP_ID = 'cl.talentodigital'
        ARTIFACT_ID = 'appmanageevents'
        VERSION = '0.0.1-RELEASE'
        PACKAGING = 'jar'
        FILE = 'target/appmanageevents-0.0.1-RELEASE.jar'
        SLACK_CHANNEL = '#aplicación-de-eventos' 
        SLACK_CREDENTIALS = 'slackToken'
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
                        configFileProvider([configFile(fileId: '554327cb-23d7-4efc-8cb0-1cce65e8b32e', variable: 'MAVEN_SETTINGS')]) {
                            sh """
                            ./mvnw deploy -s ${MAVEN_SETTINGS} -DskipTests
                            """
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed'
            slackSend(channel: env.SLACK_CHANNEL, color: '#FFFF00', message: "Pipeline completed: ${env.JOB_NAME} ${env.BUILD_NUMBER}")
        }
        success {
            echo 'Pipeline succeeded'
            slackSend(channel: env.SLACK_CHANNEL, color: 'good', message: "Pipeline succeeded: ${env.JOB_NAME} ${env.BUILD_NUMBER}")
        }
        failure {
            echo 'Pipeline failed'
            slackSend(channel: env.SLACK_CHANNEL, color: 'danger', message: "Pipeline failed: ${env.JOB_NAME} ${env.BUILD_NUMBER}")
        }
        //Aca hacemos una limpieza de imagenes de docker que no se estan ocupando
        cleanup {
            echo 'Cleaning up old Docker images'
            sh '''
                docker images --filter "reference=seiler18/mascachicles" --format "{{.ID}}" | tail -n +3 | xargs -r docker rmi -f
                docker images --filter "reference=registry.hub.docker.com/seiler18/mascachicles" --format "{{.ID}}" | tail -n +3 | xargs -r docker rmi -f
            '''
        }
    }
}
