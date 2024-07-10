pipeline {
    agent any

    environment {
        GIT_CREDENTIALS = '80fb7680-e9da-48aa-80b6-d96387fbafec' // El ID de tus credenciales en Jenkins
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/seiler18/AppManageEvents', branch: 'main', credentialsId: GIT_CREDENTIALS
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
        //Si descomentamos esta parte, la aplicación se deployara y quedara corriendo en el puerto que asignemos en nuestro docker 
        //docker run -d -p 8081:8081 appmanageevents:0.0.1-release por ejemplo
        // stage('Deploy') {
        //     steps {
        //         sh './mvnw spring-boot:run'
        //     }
        // }
    }
    
    //Acciones posteriores a la ejecución del pipeline
    post {
        always {
            echo 'Pipeline completed'
        }
    }

    //Finalizar el pipeline no es del todo necesario esto, pero solo para informar
    end {
        echo 'Pipeline finished'
    }
}
