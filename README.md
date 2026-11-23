# NutriAgrícola

Aplicación Android del proyecto semestral de Desarrollo de Aplicaciones Móviles (DSY1105). Usa Kotlin, Jetpack Compose, Material 3, MVVM, Room y Retrofit. El backend contiene un API Gateway y dos microservicios Spring Boot con PostgreSQL y Flyway.

## Integrantes

- Ariel Nuñez
- Benjamin Herrera

## Funcionalidades

- Inicio de sesión con JWT y perfiles agricultor y asesor; consulta de la cuenta y cierre de sesión.
- Predios y cultivos descargados según las asignaciones del usuario.
- Captura de análisis de suelo, agua y hojas, con cámara, galería y validaciones.
- Historial con filtros y revisión de análisis por el asesor. Un análisis con revisiones ya no puede editarse.
- Registros pendientes de sincronización, almacenamiento local y envío automático cuando existe conexión con el backend.
- Programas nutricionales visibles para ambos perfiles; el asesor crea programas mediante un formulario por pasos y administra los propios.
- Requerimientos, catálogo de fertilizantes y receta de referencia basados en https://nutrixpert.netlify.app/. Los ejemplos son ficticios y no constituyen recomendaciones de aplicación.
- Consulta del clima desde el encabezado.

## Backend

Requisitos: Java 21, Maven y acceso a PostgreSQL local o Supabase.

1. Preparar los `.env` de usuarios y registros usando sus `.env.example`. Configurar `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`; usuarios también requiere `JWT_SECRET`. No subir estos archivos a Git.
2. Para Supabase se utiliza la conexión JDBC del **Session pooler**, con SSL y el usuario completo del proyecto. Ambos servicios pueden compartir la base `postgres`: usuarios utiliza el esquema `usuarios` y registros el esquema `registros`.
3. Ejecutar los proyectos desde sus `pom.xml` en IntelliJ, o mediante `mvn spring-boot:run` desde cada carpeta:

| Proyecto | Carpeta | Puerto |
| --- | --- | --- |
| Usuarios | `backend/ms-usuarios/usuarios` | 8081 |
| Registros | `backend/ms-registros/registros` | 8082 |
| Gateway | `backend/api-gateway` | 8080 |

Flyway crea los esquemas, tablas y datos iniciales cuando corresponda según el historial de cada esquema. No modificar migraciones ya aplicadas ni recrear las bases para iniciar la aplicación. La configuración limita a tres las conexiones de cada microservicio para compartir el pool de Supabase entre integrantes.

Los datos iniciales incluyen tres predios, seis cultivos, 18 análisis y seis programas; las migraciones conservan datos manuales. Las fotografías se almacenan como archivos en el servicio de registros y sus referencias en PostgreSQL.

Cuentas exclusivamente para la presentación:

| Perfil | Correo | Contraseña |
| --- | --- | --- |
| Agricultor | farmer@test.cl | farmer12 |
| Asesor | advisor@test.cl | advisor1 |

### Principales endpoints del gateway

| Método | Ruta | Uso |
| --- | --- | --- |
| POST | `/api/v1/auth/login` | Obtener el token y perfil |
| GET | `/api/v1/users/me` | Consultar el perfil autenticado |
| GET | `/api/v1/farms` | Predios asignados |
| GET | `/api/v1/crops/farm/{farmId}` | Cultivos del predio |
| GET / POST | `/api/v1/records` | Historial paginado / crear un análisis |
| GET / PUT / DELETE | `/api/v1/records/{id}` | Consultar / editar / eliminar un análisis |
| POST | `/api/v1/reviews` | Registrar una revisión |
| GET / POST | `/api/v1/programs` | Listar / crear programas |
| GET / PUT / DELETE | `/api/v1/programs/{id}` | Consultar / editar / archivar un programa |

Excepto el login, las peticiones requieren `Authorization: Bearer <token>`. El backend aplica permisos por rol, asignación y autor.

## Android

1. Abrir `android/NutriAgricola` en Android Studio y sincronizar Gradle con JDK 21. Instalar el SDK de compilación 37.1 que solicita el proyecto.
2. Utilizar Android 8.0 (API 26) o superior.
3. Para desarrollo, ejecutar con **Run**. El emulador utiliza `http://10.0.2.2:8080/`. Un teléfono con la versión debug utiliza USB y `adb reverse tcp:8080 tcp:8080`.
4. El primer inicio de sesión requiere acceso al backend. Después se puede continuar con la sesión y los datos descargados sin conexión. Cerrar sesión requiere conectarse para volver a entrar. Crear o modificar programas requiere conexión.

## APK firmado de entrega

En Android Studio: **Build → Generate Signed App Bundle or APK → APK**, crear o seleccionar el keystore, y elegir **release**. Conservar el `.jks`, alias y contraseñas para futuras versiones; no subirlos al repositorio.

La versión release utiliza `com.nutriagricola.app.release`, por lo que puede instalarse junto a la versión debug sin eliminar su almacenamiento. Cada instalación mantiene su propia sesión y caché local.

Para esta presentación, release conecta con `http://10.233.59.242:8080/`, la dirección del PC en la zona Wi-Fi del teléfono. No necesita depuración inalámbrica. El APK puede transferirse como archivo e instalarse desde el teléfono.

Mantener encendidos el gateway, los dos microservicios y la red compartida. Supabase aloja PostgreSQL; no aloja los procesos Spring Boot. Si cambia la dirección del PC, ajustar `DEVICE_API_BASE_URL` en `app/build.gradle.kts` y el dominio permitido en `app/src/release/res/xml/release_network_security_config.xml`, y generar de nuevo el APK firmado.

## Verificación

Desde `android/NutriAgricola`: `./gradlew assembleDebug assembleRelease testDebugUnitTest` (en Windows, `gradlew.bat`). La compilación release sin configurar la firma produce un APK sin firmar; usar el asistente anterior para obtener el archivo instalable de entrega.

Desde cada proyecto backend: `mvn test`. Las pruebas de usuarios y registros usan H2, sin modificar PostgreSQL ni Supabase. Evitar ejecutar pruebas instrumentadas sobre una instalación con datos que se quieran conservar.

Comprobación manual: iniciar sesión, descargar predios y registros, abrir un análisis, entrar como asesor y revisar, consultar programas y crear uno por pasos. Para comprobar offline, descargar primero los datos, cortar la conexión, crear un registro como agricultor y después reconectar para verificar la sincronización.
