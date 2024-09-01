# Proyecto Spring Boot para Curso DevOps

## Descripción del Proyecto

Este es un proyecto desarrollado en Spring Boot Java como parte de un curso de DevOps. La aplicación implementa un CRUD (Create, Read, Update, Delete) para la gestión de eventos y salones, e integra diversas herramientas y prácticas propias de DevOps:

- Jenkins
- Nexus
- SonarQube
- Selenium

Además, la aplicación cuenta con Spring Security para la gestión de autenticación y autorización.

## Endpoints Públicos

Los siguientes endpoints están disponibles sin necesidad de autenticación:

- `/api/salones`: Consulta de salones
- `/api/eventos`: Consulta de eventos

El resto de las rutas requieren que los usuarios se registren y autentiquen. Sin autenticación, solo se mostrará la pantalla inicial de login.

## Estructura del Repositorio

El proyecto sigue el modelo de Git Flow para la gestión de ramas:

- `main`: Contiene el código listo para producción. Protegida contra cambios directos.
- `develop`: Rama de desarrollo continuo donde se integran las nuevas características.

## Flujo de Trabajo con Git Flow

### 1. Desarrollo de Nuevas Funcionalidades

```bash
# Iniciar una nueva funcionalidad
git flow feature start nombre-feature

# Desarrollar y hacer commits

# Finalizar la funcionalidad
git flow feature finish nombre-feature

# Subir cambios a develop
git push origin develop
```

### 2. Preparación de una Nueva Versión

```bash
# Iniciar una nueva release
git flow release start nombre-release

# Realizar ajustes finales

# Finalizar la release
git flow release finish nombre-release

# Subir cambios y tags
git push origin main
git push origin develop
git push --tags
```

### 3. Corrección de Errores en Producción

```bash
# Iniciar un hotfix
git flow hotfix start nombre-hotfix

# Aplicar la corrección

# Finalizar el hotfix
git flow hotfix finish nombre-hotfix

# Subir cambios y tags
git push origin main
git push origin develop
git push --tags
```

## Resumen del Flujo de Trabajo

1. Desarrollo continuo en `develop` a través de ramas de feature.
2. Preparación de versiones en ramas de release antes de fusionarlas en `main`.
3. Corrección rápida de errores críticos en ramas de hotfix directamente desde `main`.

Este flujo de trabajo permite mantener el proyecto organizado y asegura que la rama `main` siempre contenga código listo para producción, mientras que `develop` es el espacio para la integración continua de nuevas funcionalidades.