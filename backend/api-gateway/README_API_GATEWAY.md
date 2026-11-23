# Especificación Técnica: API Gateway (api-gateway)

## 1. Descripción General
El microservicio `api-gateway` actúa como el punto de entrada único para la aplicación móvil Android. Encamina las peticiones HTTP/JSON hacia los microservicios correspondientes (`users-service` y `records-service`) y gestiona la configuración de CORS.

## 2. Tecnologías Requeridas
- Java 17 o Kotlin
- Spring Boot 3.x
- Spring Cloud Gateway (Reactive / WebFlux)
- Puerto por defecto: 8080

## 3. Configuración de Rutas (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
      routes:
        # Rutas hacia users-service
        - id: users-service-auth
          uri: http://localhost:8081
          predicates:
            - Path=/api/v1/auth/**

        - id: users-service-users
          uri: http://localhost:8081
          predicates:
            - Path=/api/v1/users/**

        # Rutas hacia records-service
        - id: records-service-farms
          uri: http://localhost:8082
          predicates:
            - Path=/api/v1/farms/**

        - id: records-service-crops
          uri: http://localhost:8082
          predicates:
            - Path=/api/v1/crops/**

        - id: records-service-records
          uri: http://localhost:8082
          predicates:
            - Path=/api/v1/records/**

        - id: records-service-reviews
          uri: http://localhost:8082
          predicates:
            - Path=/api/v1/reviews/**
```

## 4. Direccionamiento para Cliente Android
La aplicación Android configurará Retrofit apuntando a una sola dirección base:
- Emulador Android: `http://10.0.2.2:8080/`
- Dispositivo Físico: `http://<IP_LOCAL_PC>:8080/`

## 5. Pruebas de Funcionamiento
- GET `http://localhost:8080/api/v1/users` -> redirige a `http://localhost:8081/api/v1/users`
- GET `http://localhost:8080/api/v1/records` -> redirige a `http://localhost:8082/api/v1/records`
