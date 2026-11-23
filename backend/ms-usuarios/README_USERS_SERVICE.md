# Especificación Técnica: Microservicio de Usuarios (users-service)

## 1. Descripción General
El microservicio `users-service` gestiona la autenticación, el registro de usuarios, las credenciales con hash y la información de los roles de usuario (FARMER y ADVISOR).

## 2. Tecnologías Requeridas
- Java 17 o Kotlin
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL (Base de datos: `nutri_users_db`)
- Puerto por defecto: 8081

## 3. Esquema de Base de Datos PostgreSQL

```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'FARMER' o 'ADVISOR'
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_credentials (
    user_id VARCHAR(36) PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    iterations INT NOT NULL DEFAULT 600000
);
```

## 4. Endpoints REST API

### 4.1 Autenticación de Usuario
- Método: `POST`
- Ruta: `/api/v1/auth/login`
- Request Body:
```json
{
  "email": "advisor@test.cl",
  "password": "advisor1"
}
```
- Response (200 OK):
```json
{
  "token": "token-ejemplo-jwt",
  "user": {
    "id": "u-advisor-001",
    "firstName": "María",
    "lastName": "González",
    "email": "advisor@test.cl",
    "role": "ADVISOR",
    "active": true
  }
}
```

### 4.2 Obtener Usuario por ID
- Método: `GET`
- Ruta: `/api/v1/users/{id}`
- Response (200 OK): Objeto JSON del usuario.

### 4.3 Listar Todos los Usuarios
- Método: `GET`
- Ruta: `/api/v1/users`
- Response (200 OK): Lista JSON de usuarios.

## 5. Datos de Prueba Iniciales (Seed Data SQL)

```sql
INSERT INTO users (id, first_name, last_name, email, role, active) 
VALUES ('u-farmer-001', 'Angel', 'Muñoz', 'farmer@test.cl', 'FARMER', true);

INSERT INTO user_credentials (user_id, password_hash, salt, iterations)
VALUES ('u-farmer-001', 'LDNO9DFzuciuvjJ6St1t/LF8Z2F264KO8iH4qWW128s=', 'QnTS/ORJYI4do4xhYwiClQ==', 600000);

INSERT INTO users (id, first_name, last_name, email, role, active) 
VALUES ('u-advisor-001', 'María', 'González', 'advisor@test.cl', 'ADVISOR', true);

INSERT INTO user_credentials (user_id, password_hash, salt, iterations)
VALUES ('u-advisor-001', 'LBpwUbrB77Hwfqz4EXiz2xa/rpDo4crcTZTrdv8J7a4=', '8T5woAkl+JZGcwCnkASY5A==', 600000);
```

## 6. Pruebas Unitarias (JUnit 5)
Incluir al menos una prueba unitaria `@SpringBootTest` para validar la consulta de usuarios por email y la verificación de contraseñas.
