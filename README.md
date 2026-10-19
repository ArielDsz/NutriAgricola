# NutriAgrícola

NutriAgrícola es una aplicación Android para registrar y consultar análisis de suelo, agua y estado foliar de cultivos. Permite organizar los registros por predio y cultivo, adjuntar fotografías y revisar los análisis desde el perfil de asesor. La información se guarda localmente para poder trabajar sin conexión.

Proyecto desarrollado para la asignatura Desarrollo de Aplicaciones Móviles (DSY1105), utilizando Kotlin, Jetpack Compose, Material 3, arquitectura MVVM y Room.

## Integrantes

- Ariel Nuñez
- Benjamin Herrera

## Funcionalidades implementadas

- Inicio y cierre de sesión con perfiles de agricultor y asesor, con restauración de sesión al abrir la aplicación.
- Selección de predio y consulta de sus cultivos, conservando el último predio seleccionado.
- Formularios de análisis de suelo, agua y foliar, con validaciones y mensajes de error.
- Captura de fotografías con la cámara y selección de imágenes desde la galería para los análisis foliares.
- Historial de registros con filtros por cultivo y tipo de análisis, y vistas de detalle.
- Consulta de análisis pendientes y registros por predio desde el perfil de asesor.
- Validación de análisis y registro de observaciones del asesor, conservando el historial de revisiones.
- Almacenamiento local de registros, revisiones y fotografías.
- Animación al mostrar los tipos de registro y transiciones entre pantallas.

## Pasos para ejecutar

1. Clonar o descargar este repositorio y abrir la carpeta raíz del proyecto en Android Studio.
2. Esperar la sincronización de Gradle. Instalar los componentes del SDK solicitados por Android Studio. El proyecto utiliza el SDK de compilación 37.1 y JDK 21 para Gradle.
3. Configurar un emulador o conectar un teléfono con Android 8.0 (API 26) o superior y depuración USB habilitada.
4. Seleccionar el módulo `app`, elegir el dispositivo y presionar **Run**.
5. Al iniciar, la aplicación prepara los usuarios, predios, cultivos y análisis de demostración. Ingresar con una de estas cuentas:

| Perfil | Correo | Contraseña |
| --- | --- | --- |
| Agricultor | farmer@test.cl | farmer12 |
| Asesor | advisor@test.cl | advisor1 |

Para probar la revisión de un análisis, crear un registro como agricultor, cerrar sesión e ingresar como asesor en el mismo dispositivo.
