# Especificación Técnica: Microservicio de Registros y Revisiones (records-service)

## 1. Descripción General
El microservicio `records-service` gestiona los predios, cultivos, análisis de suelo, agua y foliar, las fotografías asociadas y las revisiones/dictámenes emitidos por los asesores.

## 2. Tecnologías Requeridas
- Java 17 o Kotlin
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL (Base de datos: `nutri_records_db`)
- Puerto por defecto: 8082

## 3. Esquema de Base de Datos PostgreSQL

```sql
CREATE TABLE farms (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(150) NOT NULL,
    image_path VARCHAR(255),
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE crops (
    id VARCHAR(36) PRIMARY KEY,
    farm_id VARCHAR(36) NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    variety VARCHAR(100),
    area_hectares DOUBLE PRECISION,
    phenological_stage VARCHAR(100),
    image_path VARCHAR(255),
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE records (
    id VARCHAR(36) PRIMARY KEY,
    crop_id VARCHAR(36) NOT NULL REFERENCES crops(id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL,
    date VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL, -- 'SOIL', 'WATER', 'FOLIAR'
    status VARCHAR(20) NOT NULL, -- 'PENDING', 'VIEWED', 'VALIDATED', 'DELETED'
    observations TEXT
);

CREATE TABLE soil_data (
    record_id VARCHAR(36) PRIMARY KEY REFERENCES records(id) ON DELETE CASCADE,
    ph DOUBLE PRECISION,
    electrical_conductivity DOUBLE PRECISION,
    nitrogen DOUBLE PRECISION,
    phosphorus DOUBLE PRECISION,
    potassium DOUBLE PRECISION,
    calcium DOUBLE PRECISION,
    magnesium DOUBLE PRECISION,
    organic_matter DOUBLE PRECISION
);

CREATE TABLE water_data (
    record_id VARCHAR(36) PRIMARY KEY REFERENCES records(id) ON DELETE CASCADE,
    ph DOUBLE PRECISION,
    electrical_conductivity DOUBLE PRECISION,
    nitrogen DOUBLE PRECISION,
    phosphorus DOUBLE PRECISION,
    potassium DOUBLE PRECISION,
    calcium DOUBLE PRECISION,
    magnesium DOUBLE PRECISION,
    water_source VARCHAR(150)
);

CREATE TABLE foliar_data (
    record_id VARCHAR(36) PRIMARY KEY REFERENCES records(id) ON DELETE CASCADE,
    nitrogen DOUBLE PRECISION,
    phosphorus DOUBLE PRECISION,
    potassium DOUBLE PRECISION,
    calcium DOUBLE PRECISION,
    magnesium DOUBLE PRECISION,
    symptoms TEXT
);

CREATE TABLE foliar_photos (
    id VARCHAR(36) PRIMARY KEY,
    record_id VARCHAR(36) NOT NULL REFERENCES records(id) ON DELETE CASCADE,
    path TEXT NOT NULL
);

CREATE TABLE record_reviews (
    id VARCHAR(36) PRIMARY KEY,
    record_id VARCHAR(36) NOT NULL REFERENCES records(id) ON DELETE CASCADE,
    advisor_id VARCHAR(36) NOT NULL,
    advisor_name VARCHAR(150) NOT NULL,
    date VARCHAR(30) NOT NULL,
    result VARCHAR(20) NOT NULL, -- 'VIEWED', 'VALIDATED'
    comment TEXT NOT NULL
);
```

## 4. Endpoints REST API

### 4.1 Obtener Todos los Predios
- Método: `GET`
- Ruta: `/api/v1/farms`

### 4.2 Obtener Cultivos por Predio
- Método: `GET`
- Ruta: `/api/v1/crops/farm/{farmId}`

### 4.3 Obtener Análisis por Estado
- Método: `GET`
- Ruta: `/api/v1/records/status/{status}` (ej. `/api/v1/records/status/PENDING`)

### 4.4 Crear Nuevo Análisis (Farmer)
- Método: `POST`
- Ruta: `/api/v1/records`
- Request Body:
```json
{
  "id": "rec-101",
  "cropId": "crop-001",
  "userId": "u-farmer-001",
  "date": "2026-10-18",
  "type": "SOIL",
  "status": "PENDING",
  "observations": "Muestra sector norte",
  "soilData": {
    "ph": 6.5,
    "electricalConductivity": 1.2,
    "nitrogen": 35.0,
    "phosphorus": 18.0,
    "potassium": 160.0,
    "calcium": 1100.0,
    "magnesium": 170.0,
    "organicMatter": 2.8
  }
}
```

### 4.5 Guardar Dictamen de Asesoría (Advisor)
- Método: `POST`
- Ruta: `/api/v1/reviews`
- Request Body:
```json
{
  "id": "rev-501",
  "recordId": "rec-101",
  "advisorId": "u-advisor-001",
  "advisorName": "María González",
  "date": "2026-10-18T16:00:00",
  "result": "VALIDATED",
  "comment": "Análisis validado correctamente."
}
```
- Comportamiento: Debe ejecutarse en una transacción `@Transactional` que guarda el registro en `record_reviews` y actualiza el atributo `status` en la tabla `records`.

## 5. Pruebas Unitarias (JUnit 5)
Incluir al menos una prueba unitaria `@SpringBootTest` para verificar la inserción transaccional de revisiones y la actualización del estado del registro.
