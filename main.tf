# Configura el proveedor Docker con la fuente correcta
terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 2.0" # Ajusta la versión según sea necesario
    }
  }
}

# Configura el proveedor Docker
provider "docker" {}

# Jenkins Service
resource "docker_image" "jenkins_image" {
  name = "jenkins/jenkins:latest"
}

resource "docker_container" "jenkins_container" {
  image = docker_image.jenkins_image.name
  name  = "jenkins"
  ports {
    internal = 8080
    external = 8080
  }
  ports {
    internal = 50000
    external = 50000
  }
  volumes {
    host_path      = "/var/lib/docker/volumes/jenkins_data"
    container_path = "/var/jenkins_home"
  }
}

# SonarQube Service
resource "docker_image" "sonarqube_image" {
  name = "sonarqube:10.6-community"
}

resource "docker_container" "sonarqube_container" {
  image = docker_image.sonarqube_image.name
  name  = "sonarqube"
  ports {
    internal = 9000
    external = 9000
  }
  volumes {
    host_path      = "/var/lib/docker/volumes/sonarqube_data"
    container_path = "/opt/sonarqube/data"
  }
  env = [
    "SONAR_JDBC_USERNAME=sonar",
    "SONAR_JDBC_PASSWORD=sonar",
    "SONAR_JDBC_URL=jdbc:postgresql://db:5432/sonar"
  ]
  depends_on = [docker_container.db]
}

# PostgreSQL Service for SonarQube
resource "docker_image" "postgres_image" {
  name = "postgres:13"
}

resource "docker_container" "db" {
  image = docker_image.postgres_image.name
  name  = "db"
  env = [
    "POSTGRES_USER=sonar",
    "POSTGRES_PASSWORD=sonar",
    "POSTGRES_DB=sonar"
  ]
  volumes {
    host_path      = "/var/lib/docker/volumes/postgresql_data"
    container_path = "/var/lib/postgresql/data"
  }
}

# Nexus Service
resource "docker_image" "nexus_image" {
  name = "sonatype/nexus3"
}

resource "docker_container" "nexus_container" {
  image = docker_image.nexus_image.name
  name  = "nexus"
  ports {
    internal = 8081
    external = 8081
  }
  volumes {
    host_path      = "/var/lib/docker/volumes/nexus_data"
    container_path = "/nexus-data"
  }
}

# MongoDB Service
resource "docker_image" "mongodb_image" {
  name = "mongo:4.4"
}

resource "docker_container" "mongodb_container" {
  image = docker_image.mongodb_image.name
  name  = "mongodb"
  volumes {
    host_path      = "/var/lib/docker/volumes/mongo_data"
    container_path = "/data/db"
  }
}
