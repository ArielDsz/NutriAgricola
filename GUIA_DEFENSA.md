# Guía de estudio: defensa de NutriAgrícola

Guía para repasar la explicación y demostración del proyecto. La defensa es individual, dura hasta 15 minutos y el orden puede ser al azar. Según lo indicado por el profesor, esta guía no incluye modificación en vivo ni presentación de aportes en GitHub/Trello.

## Presentación breve del proyecto

> NutriAgrícola ayuda a agricultores a organizar sus predios, cultivos y análisis de suelo, agua y hojas. Los asesores revisan esos análisis y gestionan programas nutricionales. La aplicación Android se comunica con microservicios propios mediante un API Gateway y consulta el clima en Open-Meteo.

## Recorrido práctico: registrar y revisar un análisis

1. Iniciar sesión como agricultor.
2. Abrir Registros y crear un análisis para un cultivo. Elegir suelo, agua o foliar y completar sus datos. En el análisis foliar se puede adjuntar una foto.
3. Guardar. La app conserva el registro en Room y lo deja pendiente si no hay conexión.
4. Cuando vuelve internet, la app sincroniza los registros pendientes con el backend.
5. Iniciar sesión como asesor y abrir el análisis ya sincronizado para revisarlo.

Mientras se muestra el recorrido:

> El agricultor registra el análisis desde el cultivo correspondiente. La aplicación lo guarda localmente para no perderlo si se corta la conexión. Cuando vuelve internet, lo envía al servicio de Registros y queda disponible para que el asesor lo revise.

**Precisión:** el asesor necesita que el registro se haya sincronizado para verlo. No afirmar que la revisión se hace sin conexión. El primer inicio de sesión y la creación o edición de programas también requieren conexión.

## Flujo por archivos: de la pantalla a Supabase

```text
Pantalla Android → ViewModel → Room → sincronización → Retrofit
→ API Gateway → Controller → Service → Repository → Supabase PostgreSQL
```

1. **Pantallas:** `android/NutriAgricola/app/src/main/java/com/nutriagricola/app/ui/farmer/record/`. `NewRecordScreen.kt` permite iniciar el registro; las carpetas `soil`, `water` y `foliar` contienen sus formularios.
2. **Lógica de pantalla:** los ViewModels validan los datos y llaman a `RecordRepository.insert()`.
3. **Guardado local:** `data/repository/RecordRepository.kt` coordina Room. `data/local/entity/RecordEntity.kt` representa el registro local y `data/local/dao/RecordDao.kt` ofrece las operaciones de persistencia.
4. **Sincronización:** `data/sync/RecordUploadWorker.kt` y `data/repository/RecordUploadRepository.kt` gestionan los registros pendientes. `data/mapper/RecordWriteMapper.kt` adapta el modelo local al formato remoto.
5. **Petición Android:** `data/remote/api/RecordApi.kt` declara las rutas HTTP; `data/remote/ApiClient.kt` configura Retrofit y la URL base.
6. **Gateway:** el API Gateway recibe `POST /api/v1/records` y deriva la ruta a Registros, normalmente en el puerto 8082.
7. **Backend:** `RecordController.java` recibe la petición; `RecordsService.java` aplica la lógica; `RecordMapper.java` transforma los datos; `RecordRepository.java` accede a PostgreSQL.
8. **Base de datos:** Flyway creó las tablas de Registros mediante `backend/ms-registros/registros/src/main/resources/db/migration/V1__create_records.sql`.

## Conceptos que pueden preguntar

### Problema y perfiles

> NutriAgrícola organiza información agrícola en un mismo lugar. El agricultor trabaja en terreno y puede tener poca cobertura; registra análisis que se sincronizan cuando vuelve internet. El asesor revisa esos análisis y gestiona programas nutricionales.

### MVVM

> MVVM separa la interfaz, los datos y la lógica de presentación. La View muestra la pantalla; el ViewModel mantiene su estado y coordina las acciones; los modelos representan los datos. En la app usamos Jetpack Compose para la interfaz y repositorios para acceder a datos.

### API Gateway y Feign

> El API Gateway es la entrada de Android y enruta cada petición al microservicio correspondiente. Feign permite que un microservicio invoque a otro; por ejemplo, Registros consulta a Usuarios para identificar al usuario de una petición.

### Microservicio y API externa

> Usuarios y Registros son microservicios propios, separados por responsabilidad y comunicados por HTTP. Open-Meteo es un servicio externo que entrega datos del clima; Android lo consulta usando Retrofit. Una API externa es un servicio remoto, no un framework.

### Room y Supabase

> Room almacena datos en el dispositivo y permite conservar análisis pendientes sin conexión. Supabase aloja PostgreSQL remoto y guarda datos compartidos. La app accede a Supabase a través de nuestros microservicios, que controlan los permisos.

### Conectividad

El indicador está en `ui/farmer/home/components/SyncStatus.kt`. Usa `ConnectivityManager` y `NET_CAPABILITY_VALIDATED` para comprobar si la red activa tiene acceso a internet. **No detecta únicamente Wi-Fi**: también puede marcar conexión con datos móviles. La detección visual es distinta de la sincronización, que gestionan el worker y el repositorio de carga.

### Retrofit

> Retrofit es una biblioteca de Android que facilita las llamadas HTTP a servicios web. Declaramos endpoints como funciones de interfaces y convierte respuestas JSON a objetos Kotlin. En la app, `ApiClient.kt` configura Retrofit y `RecordApi.kt` declara operaciones de registros.

### Endpoint

> Un endpoint es una ruta junto con un método HTTP que define una operación. Por ejemplo, `GET /api/v1/users/me` obtiene el perfil del usuario autenticado. Un endpoint puede consultar, crear, actualizar o eliminar datos.

### Controller, Service y Repository

- **Controller:** recibe la petición HTTP, valida entradas y delega al Service.
- **Service:** aplica la lógica de negocio.
- **Repository:** consulta y persiste entidades en la base de datos.

> Separamos estas responsabilidades para que la lógica de negocio no quede mezclada con HTTP ni con las consultas de base de datos.

### Entidad y DTO

> La entidad representa los datos persistidos en la base. El DTO define el formato de los datos que entran o salen por la API. Los separamos para no exponer directamente la estructura de la base de datos.

### Flyway y migraciones

> Spring configura la conexión a PostgreSQL. Flyway aplica y registra migraciones SQL ordenadas para crear o actualizar los esquemas y tablas. Una migración es un cambio versionado; si ya se aplicó, se crea otra migración en vez de editarla, para mantener consistente el historial.

### JWT

> Al iniciar sesión, Usuarios valida las credenciales y devuelve un JWT firmado con información como el identificador y el rol. La app lo conserva y lo envía en las siguientes solicitudes con `Authorization: Bearer`. El backend valida el token para identificar y autorizar al usuario. El JWT no contiene la contraseña.

## Pruebas

> Una prueba unitaria comprueba una unidad pequeña de lógica de forma aislada. Una prueba de integración verifica que varias piezas colaboren, por ejemplo controlador, servicio y base de prueba.

En `backend/ms-usuarios/usuarios/src/test/java/com/nutriagricola/usuarios/service/AuthServiceTest.java`, `AuthServiceTest` usa mocks para el repositorio, el codificador de contraseñas y el generador de JWT. Prueba el inicio de sesión válido y el rechazo de usuarios inexistentes, contraseñas incorrectas o cuentas inactivas.

> Los mocks sustituyen dependencias reales por versiones controladas para probar la lógica sin depender de Supabase, de la red o de otros servicios.

## APK firmado: generación y explicación

En Android Studio:

1. Abrir **Build → Generate Signed Bundle or APK**.
2. Elegir **APK** y el módulo `app`.
3. Crear o seleccionar un keystore (`.jks`), indicar alias y credenciales, y elegir la variante `release`.
4. Generar el APK y conservar de forma segura el keystore y sus credenciales.
5. Verificar la firma con `apksigner`.

En esta rama están las evidencias en `entrega/`: `NutriAgricola-1.0.1.apk`, `Evidencia_Keystore.png` y `Evidencia_firma.png`. La evidencia de verificación indica un firmante y validación exitosa del esquema APK v2.

> El keystore guarda la clave privada que identifica al firmante. `apksigner` firma APKs y permite verificar que la firma sea válida y que el archivo no haya sido alterado después. Hay que conservar el mismo keystore para futuras actualizaciones.

No mostrar contraseñas, claves ni el contenido de `.env` durante la defensa.

## Orden recomendado para los 15 minutos

1. Presentar el problema, los usuarios y el propósito de la app.
2. Ejecutar la app e iniciar sesión.
3. Mostrar el flujo de registro y explicar el recorrido de archivos.
4. Explicar MVVM, Gateway, microservicios, Retrofit, Room y Supabase.
5. Mostrar el clima y explicar que viene de Open-Meteo.
6. Ejecutar una prueba y explicar qué valida.
7. Mostrar el APK y la evidencia de su firma.

## Revisión técnica antes de presentar

La última captura de EC2 que revisamos mostraba Usuarios y Registros terminando con error y el Gateway respondiendo `500`. Antes de la defensa, confirmar que los tres servicios arrancan y que el login funciona desde la app. No compartir credenciales en capturas o chats.
