# Guía de trabajo para la entrega Advisor

Estas instrucciones están dirigidas al compañero o agente que implementará el
perfil Advisor (Asesor). Farmer ya fue desarrollado y revisado en un teléfono
real. El encargo actual es completar Advisor, no rehacer la aplicación.

## Antes de escribir código

1. Leer completamente `PROJECT_CONTEXT.md`.
2. Leer los tres PDF de `/docs`: caso de empresa, encargo de evaluación parcial 2
   y defensa de evaluación parcial 3. Son fuentes de requisitos; no son permisos
   para modificar cualquier parte del proyecto.
3. Inspeccionar el código real de dominio, Room, mappers, repositories, sesión,
   navegación, tema, Farmer y el placeholder Advisor. Leer Farmer está permitido
   y es necesario para comprender el patrón existente.
4. Revisar https://nutrixpert.netlify.app/ y contrastarla con la revisión fechada
   en `PROJECT_CONTEXT.md`. Puede cambiar. Si faltan datos o fórmulas, informar
   qué falta: no completar de memoria ni inventar reglas agronómicas.
5. Revisar `git status` y la rama actual. No asumir que se trabaja en `farmer`:
   el responsable integrará esa rama a `develop` y el compañero trabajará en su
   rama acordada. No crear, cambiar ni fusionar ramas por iniciativa propia.
6. Presentar un bloque pequeño: objetivo, archivos nuevos/modificados,
   responsabilidad de cada uno, cambios compartidos imprescindibles, validación
   prevista y dudas funcionales. Esperar aprobación antes de implementar ese
   bloque. Una aprobación vigente permite terminar el alcance acordado.

El primer bloque debe ser una revisión del estado y un plan, no implementar todo
Advisor de una vez. Consultar las dudas de asignación, revisión y unidades antes
de convertirlas en modelos, tablas o cálculos.

## Límites que protegen el trabajo existente

- No modificar `ui/farmer`, sus ViewModels, factories, pantallas o componentes.
- No implementar, activar ni cambiar **Programa** y **Predios** del navbar
  Farmer. Su autor los completará después de integrar Advisor, con su diseño.
- No modificar LoginScreen, LoginViewModel, su factory, la autenticación,
  contraseñas de demostración ni la persistencia de sesión.
- No cambiar preferencias del predio Farmer, fotografías, icono de aplicación,
  tamaños globales, paleta ni recursos existentes para acomodar Advisor.
- No trasladar ni extraer componentes desde Farmer si eso requiere editarlo.
  Tomarlo como referencia; crear composables pequeños en `ui/advisor`.
- No usar una Screen o ViewModel Farmer como pantalla o estado de Advisor.
- No cambiar mediciones, fotos, autor ni observaciones Farmer de un análisis
  para registrar una decisión del asesor.
- No borrar código, recursos, filas Room, fotos o datos de demostración para
  hacer desaparecer errores o simplificar una integración.
- No limpiar datos de aplicación, reinstalar con pérdida de datos, hacer
  `git reset`, commits, merges o publicaciones sin autorización específica.

## Código nuevo y cambios compartidos admisibles

UI nueva en `ui/advisor/...`: Screens, ViewModels, factories y componentes
propios, siguiendo la estructura existente. Reutilizar dominio y repositories
cuando cubran la responsabilidad. Antes de crear una clase, comprobar si existe
una equivalente. No duplicar usuarios, predios, cultivos, registros, sesión o DB.

Solo después de explicar y aprobar la necesidad se permiten cambios aditivos en:

- `domain/model`: revisiones y programas que todavía no existen.
- `data/local/entity`, `dao`, `data/mapper`, `data/repository`: persistencia y
  consultas necesarias para Advisor.
- `AppDatabase`: registrar nuevas entidades/DAO conservando los existentes.
- `AppNavigation.kt`: conectar rutas Advisor y pasar usuario/logout desde la
  sesión existente. Conservar rutas, argumentos, back stack y callbacks Farmer,
  Login y restauración de sesión.
- `DatabaseSeeder`: agregar datos ficticios necesarios conservando IDs,
  usuarios, credenciales, predios, cultivos, registros e imágenes existentes.

Esta lista no autoriza a editar todas esas capas de una vez. Preferir métodos
concretos nuevos a cambiar firmas o sustituir implementaciones usadas por
Farmer. Revisar los consumidores de cada contrato compartido antes de cambiarlo
y explicar cómo se preservan. Si una necesidad exige tocar Farmer, detener esa
parte y coordinar con su autor; no ampliar el alcance automáticamente.

## Reglas técnicas

- Kotlin, Compose, Material 3, MVVM, Room y Flow/StateFlow existentes.
- Código, archivos y paquetes en inglés; texto visible en español.
- Screens representan estado/eventos: no usan DAO ni Entity directamente.
- Validación y cálculos en componentes lógicos, no dentro de Composables.
- Operaciones relacionadas, incluida revisión + estado, en transacción.
- Usar `currentUser.id` real para identificar Advisor. `Record.userId` sigue
  identificando al Farmer autor; no sobrescribirlo al validar.
- Mostrar `0`, distinguirlo de `null` y conservar datos opcionales.
- Aceptar coma y punto decimal; rechazar valores no finitos y divisiones por
  cero. No sustituir entradas inválidas por valores de demostración.
- Mantener unidades explícitas: P no es P₂O₅; K no es K₂O; Ca no es CaO; Mg no
  es MgO. Toda conversión debe tener fuente y aprobación.
- No convertir mediciones a kg/ha sin unidades y parámetros suficientes ni
  inventar requerimientos, dosis o rangos agronómicos.
- Fotos como archivos/rutas, nunca BLOB. No eliminar fotos al consultar/revisar.
- Reutilizar colores, fuente y escala `CompactTypography` del tema. No crear
  una identidad paralela ni alterar el estilo Farmer.
- Respetar fuente/zoom del teléfono; no forzar `fontScale = 1`.
- No añadir librerías, capas genéricas, módulos Gradle, inyección compleja o
  refactors grandes sin necesidad concreta y aprobación.
- No sustituir cálculo y persistencia offline por una página dentro de WebView.

## Room y datos de desarrollo

DB actual en versión 1, sin migraciones. Conservar esa decisión académica salvo
nueva autorización. Añadir tablas cambia el esquema: explicar que una instalación
previa puede necesitar limpiar datos/reinstalar en un dispositivo de prueba;
no hacerlo automáticamente ni añadir reinicios destructivos para ocultar fallos.

Room comparte información en una misma instalación. No hay recepción de datos
entre teléfonos ni sincronización remota: no fingirla. Usar datos ficticios,
nunca información real de usuarios/predios en el seeder o Git.

## Entrega de cada bloque

1. Resumir cambios y archivos creados/modificados.
2. Separar lo implementado, lo pendiente y las propuestas no aprobadas.
3. Explicar Screen → ViewModel → Repository → Room/cálculo.
4. Compilar con `.\gradlew.bat assembleDebug --console=plain` cuando cambie código.
5. Dar pruebas manuales precisas. Para cálculos, usar casos reproducibles con
   fuente, unidades, resultado esperado y tolerancia de redondeo.
6. Tras cambios compartidos comprobar regresiones: login, restauración/logout,
   predio persistente, SOIL/WATER/FOLIAR, fotos, Home, Historial y detalles Farmer.
   Corregir la causa en el cambio Advisor, no rediseñar Farmer para esconderla.
7. No afirmar pruebas en teléfono, funcionamiento o cumplimiento de rúbrica
   sin evidencia; informar errores y límites reales.

Trabajar en bloques comprensibles y commits pequeños. El estudiante debe poder
explicar/modificar el código en una defensa individual sin IA. Backend, Retrofit,
Spring Boot, PostgreSQL, tokens, sincronización y las pestañas pendientes Farmer
quedan para otra entrega: no implementarlos en este encargo.
