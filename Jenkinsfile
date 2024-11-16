pipeline {
    agent any // Define el agente en el que se ejecutará el pipeline, en este caso, cualquier nodo disponible

    environment {
        // Definición de variables de entorno utilizadas en diferentes etapas
        GIT_CREDENTIALS = '80fb7680-e9da-48aa-80b6-d96387fbafec'  // Credenciales de Git
        DOCKER_HUB_CREDENTIALS = 'docker-hub-credentials' // Credenciales para Docker Hub
        DOCKER_IMAGE_TAG = "seiler18/mascachicles:AppFinalRelease-${env.BUILD_NUMBER}"  // Etiqueta para la imagen Docker con el número de build
        SONARQUBE_SERVER = 'ProbandoSonar'  // Nombre del servidor de SonarQube
        SONARQUBE_TOKEN = credentials('ProbandoSonar')  // Token de autenticación de SonarQube
        NEXUS_CREDENTIALS = 'NexusLogin'  // Credenciales de Nexus
        GROUP_ID = 'cl.talentodigital'  // Grupo del proyecto
        ARTIFACT_ID = 'appmanageevents'  // ID del artefacto
        VERSION = '0.0.1-RELEASE'  // Versión del artefacto
        PACKAGING = 'jar'  // Tipo de empaquetado (en este caso, un archivo JAR)
        FILE = 'target/appmanageevents-0.0.1-RELEASE.jar'  // Ruta al archivo JAR generado
        SLACK_CHANNEL = '#aplicación-de-eventos' // Canal de Slack donde se enviarán notificaciones
        SLACK_CREDENTIALS = 'slackToken'  // Credenciales de Slack
    }

    stages {
        // Etapa de checkout del código fuente desde Git
        stage('Checkout') {
            when {
                branch 'main' // Solo se ejecuta en la rama 'main'
            }
            steps {
                git url: 'https://github.com/seiler18/AppManageEvents', branch: 'main', credentialsId: GIT_CREDENTIALS  // Realiza el checkout del repositorio con las credenciales
            }
        }

        // Etapa de construcción y empaquetado con Maven
        stage('Build and Package with Maven') {
            steps {
                sh './mvnw clean package -DskipTests'  // Ejecuta Maven para limpiar y empaquetar el proyecto, omitiendo las pruebas
            }
        }

        // Etapa de ejecución de pruebas unitarias
        stage('Run Unit Tests') {
            steps {
                sh './mvnw test'  // Ejecuta las pruebas unitarias con Maven
                junit '**/target/surefire-reports/*.xml' // Publica los resultados de las pruebas en formato XML
            }
        }

        // Etapa de generación de informe de cobertura de código con JaCoCo
        stage('Generate Code Coverage Report') {
            steps {
                sh './mvnw jacoco:report'  // Ejecuta el reporte de cobertura de código utilizando JaCoCo
            }
        }

        // Etapa para archivar los informes generados (en este caso, los de JaCoCo)
        stage('Archive Reports') {
            steps {
                archiveArtifacts artifacts: 'target/site/jacoco/*.html', allowEmptyArchive: true  // Archiva los informes HTML generados por JaCoCo
            }
        }

        // Etapa de análisis con SonarQube
        stage('SonarQube Analysis') {
            steps {
                script {
                    withSonarQubeEnv(SONARQUBE_SERVER) {  // Configura el entorno de SonarQube
                        sh './mvnw sonar:sonar -Dsonar.token=${SONARQUBE_TOKEN}'  // Ejecuta el análisis de SonarQube usando el token de autenticación
                    }
                }
            }
        }

        // Etapa para esperar el resultado del Quality Gate de SonarQube
        stage('Wait for Quality Gate') {
            steps {
                timeout(time: 1, unit: 'MINUTES') {  // Establece un tiempo de espera de 1 minuto
                    waitForQualityGate abortPipeline: false  // Espera el Quality Gate sin abortar el pipeline si falla
                }
            }
        }

        // Etapa de construcción de la imagen Docker
        stage('Build Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', DOCKER_HUB_CREDENTIALS) {  // Autenticación con Docker Hub
                        def dockerImage = docker.build("${DOCKER_IMAGE_TAG}", ".")  // Construye la imagen Docker con la etiqueta definida
                        dockerImage.push()  // Empuja la imagen al repositorio Docker Hub
                    }
                }
            }
        }

        // Etapa de publicación del artefacto en Nexus
        stage('Publish to Nexus') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: NEXUS_CREDENTIALS, usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {  // Autenticación en Nexus
                        configFileProvider([configFile(fileId: '554327cb-23d7-4efc-8cb0-1cce65e8b32e', variable: 'MAVEN_SETTINGS')]) {  // Proporciona la configuración de Maven
                            sh """
                            ./mvnw deploy -s ${MAVEN_SETTINGS} -DskipTests  // Despliega el artefacto en Nexus, omitiendo las pruebas
                            """
                        }
                    }
                }
            }
        }
    }

    post {
        // Etapa para siempre enviar una notificación de finalización
        always {
            echo 'Pipeline completed'  // Imprime que el pipeline ha finalizado
            slackSend(channel: env.SLACK_CHANNEL, color: '#FFFF00', message: "Pipeline completed: ${env.JOB_NAME} ${env.BUILD_NUMBER}")  // Envia una notificación a Slack con el estado de finalización
        }
        
        // Etapa de éxito
        success {
            echo 'Pipeline succeeded'  // Imprime que el pipeline fue exitoso
            slackSend(channel: env.SLACK_CHANNEL, color: 'good', message: "Pipeline succeeded: ${env.JOB_NAME} ${env.BUILD_NUMBER}")  // Notifica éxito en Slack
        }

        // Etapa de fallo
        failure {
            echo 'Pipeline failed'  // Imprime que el pipeline falló
            slackSend(channel: env.SLACK_CHANNEL, color: 'danger', message: "Pipeline failed: ${env.JOB_NAME} ${env.BUILD_NUMBER}")  // Notifica fallo en Slack
        }

        // Etapa de limpieza de imágenes Docker antiguas
        cleanup {
            echo 'Cleaning up old Docker images'  // Imprime que se están limpiando imágenes Docker antiguas
            sh '''
                docker images --filter "reference=seiler18/mascachicles" --format "{{.ID}}" | tail -n +3 | xargs -r docker rmi -f  // Elimina las imágenes antiguas que no están siendo utilizadas
                docker images --filter "reference=registry.hub.docker.com/seiler18/mascachicles" --format "{{.ID}}" | tail -n +3 | xargs -r docker rmi -f  // Elimina las imágenes del Docker Hub
            '''
        }
    }
}
