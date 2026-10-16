# NutriAgrícola - Contexto del proyecto

NutriAgrícola es una aplicación Android desarrollada en Kotlin con Jetpack Compose,
Material 3, MVVM y Room.

El objetivo actual es completar correctamente todo el flujo del perfil Farmer
(Agricultor), dejando la arquitectura preparada para integrar posteriormente
Advisor, backend, autenticación remota y sincronización.

El código actual es la principal fuente de verdad técnica. Antes de implementar
algo nuevo se deben revisar los modelos, entidades, DAO, repositories, mappers,
ViewModels, navegación, componentes y tema ya existentes.

Si este documento entra en conflicto con una implementación reciente, se debe
señalar la contradicción antes de modificar arquitectura o modelos.

---

# Convenciones generales

- Código, clases, funciones, variables, paquetes y archivos: inglés.
- Texto visible para el usuario: español.
- Farmer = Agricultor.
- Advisor = Asesor.
- Mantener separados `ui/farmer` y `ui/advisor`.
- Compartir únicamente modelos, datos o componentes realmente comunes.
- Utilizar Material 3 y el tema definido en `ui/theme`.
- No introducir una identidad visual distinta sin necesidad.
- Priorizar interfaces limpias, simples, legibles y consistentes.
- Evitar complejidad arquitectónica o visual que no aporte al proyecto.

---

# Arquitectura actual

Tecnologías principales:

- Kotlin.
- Jetpack Compose.
- Material 3.
- MVVM.
- Room.
- Navigation Compose.

Para esta primera versión Room es la fuente de datos local.

La aplicación debe mantenerse preparada para funcionar offline.

Posteriormente se incorporará:

Android
→ Retrofit
→ Spring Boot
→ PostgreSQL.

Android nunca debe conectarse directamente a PostgreSQL.

Room seguirá utilizándose posteriormente como almacenamiento local y soporte
offline, mientras que el backend será la fuente remota de información.

---

# Autenticación y sesión

La aplicación tendrá login mediante:

- correo;
- contraseña.

En esta primera versión la autenticación se realizará localmente.

El usuario NO selecciona manualmente si es Farmer o Advisor.

Flujo:

correo + contraseña
→ validar credenciales
→ obtener User
→ consultar User.role
→ iniciar sesión
→ navegar al flujo correspondiente.

Las credenciales locales deben mantenerse separadas del modelo `User`.

No guardar la contraseña directamente dentro de `User`.

No guardar contraseñas en texto plano.

Para la implementación local se puede utilizar una entidad específica de
credenciales asociada mediante `userId`, almacenando solamente la representación
segura necesaria para validar la contraseña.

La lógica de autenticación debe estar encapsulada para que posteriormente pueda
reemplazarse la validación local por una llamada al backend sin modificar el
resto de la aplicación.

Después de iniciar sesión debe existir un usuario autenticado disponible durante
la sesión de la aplicación.

Como mínimo se debe poder acceder a:

- id;
- firstName;
- lastName;
- email;
- role.

El `User.id` autenticado será utilizado al crear `Record` y otras operaciones
que requieran conocer al usuario actual.

La sesión local puede mantenerse en memoria durante esta primera versión.
Persistencia de sesión, tokens y refresh tokens se implementarán cuando exista
el backend si resultan necesarios.

---

# Usuario actual e interfaz

La interfaz nunca debe depender de datos de usuario hardcodeados cuando exista
una sesión autenticada.

El botón circular de usuario mostrado en el Home debe mostrar las iniciales
reales del usuario autenticado.

Ejemplos:

Ariel Muñoz → AM
María González → MG

Las iniciales deben obtenerse a partir de `firstName` y `lastName`.

Actualmente el botón puede mostrar únicamente las iniciales.

Posteriormente podrá abrir un menú o pantalla con opciones de cuenta, perfil y
cierre de sesión.

---

# Relaciones principales

Un Farm puede contener varios Crop.

Un Crop pertenece a un Farm.

Información propia de Crop:

- nombre;
- variedad;
- superficie;
- etapa fenológica;
- imagen;
- estado activo cuando corresponda.

Los datos nutricionales NO pertenecen directamente a Crop.

Los análisis y mediciones se almacenan mediante Record.

Cada Record pertenece a:

- un Crop;
- un User.

Tipos de registro:

- SOIL;
- WATER;
- FOLIAR.

Los datos específicos de cada tipo se mantienen separados utilizando la
estructura existente del proyecto.

Las fotografías no deben almacenarse como BLOB dentro de Room.

Las imágenes se almacenarán como archivos y Room guardará solamente su ruta o
URI.

Cuando no exista una imagen personalizada se utilizará una imagen por defecto.

---

# Flujo general Farmer

Login
→ Home
→ trabajar sobre un predio seleccionado
→ consultar cultivos
→ crear registros
→ consultar historial
→ consultar programas nutricionales
→ administrar predios y cultivos.

La bottom navigation del Farmer contiene:

- Inicio
- Historial
- Programa
- Predios

Los flujos secundarios, como Nuevo registro, formularios o edición, no necesitan
mostrar la bottom navigation cuando forman parte de una tarea puntual.

---

# Pantallas Farmer

## 1. Login

Debe permitir ingresar:

- correo;
- contraseña.

No debe existir selector manual de rol.

Al presionar iniciar sesión:

1. validar credenciales;
2. obtener el User correspondiente;
3. establecerlo como usuario autenticado;
4. utilizar `role` para decidir la navegación.

Si las credenciales no son correctas se debe mostrar un error comprensible.

La pantalla actual puede mantener el diseño visual existente.

En el futuro la misma pantalla utilizará el backend en lugar de Room.

---

## 2. Home

Es la pantalla principal del Farmer.

Debe mostrar:

1. Header.
2. Predio seleccionado.
3. Estado de conexión.
4. Cultivos del predio.
5. Botón `Nuevo registro`.
6. Historial reciente.
7. Bottom navigation.

### Header

Debe mostrar:

- logo;
- `NutriAgrícola`;
- botón circular de usuario.

El botón de usuario debe mostrar las iniciales calculadas a partir del Farmer
autenticado.

No utilizar iniciales hardcodeadas.

Posteriormente este botón permitirá acceder a opciones de cuenta.

### Predio

Debe mostrar el predio actualmente seleccionado.

El predio debe provenir de Room.

La última selección de predio se conserva en DataStore Preferences mediante su
`farmId`, asociado al `User.id` autenticado. Al abrir el flujo Farmer se restaura
esa selección si el predio sigue disponible; de lo contrario se utiliza el
primer predio disponible. `FarmerContextViewModel` mantiene la única fuente del
predio seleccionado durante el flujo.

En la implementación actual puede existir un predio inicial seleccionado, pero
la arquitectura debe permitir cambiarlo posteriormente.

### Estado de conexión

Debe indicar realmente:

- En línea;
- Sin conexión.

No debe fingir sincronización con backend mientras éste no exista.

La sincronización remota se implementará posteriormente.

### Cultivos

Los cultivos mostrados deben pertenecer al predio seleccionado.

Cada tarjeta puede mostrar:

- imagen;
- nombre;
- variedad;
- etapa fenológica;
- superficie.

No mostrar datos nutricionales como si fueran propiedades permanentes del Crop.

### Nuevo registro

Debe abrir el flujo de creación de Record correspondiente al predio actual.

### Historial reciente

Debe mostrar una cantidad pequeña de registros recientes.

Debe reutilizar los mismos datos que posteriormente utilizará la pantalla
Historial completa.

---

## 3. Nuevo registro - selección

Flujo:

Nuevo registro
→ seleccionar cultivo
→ seleccionar tipo de registro
→ continuar al formulario correspondiente.

El predio actual debe mostrarse de manera compacta como contexto.

Inicialmente se muestra el selector de cultivo.

Después de seleccionar un Crop aparecen:

- Análisis de suelo;
- Análisis de agua;
- Análisis foliar.

El botón `Continuar` solamente se habilita cuando existe:

- Crop seleccionado;
- RecordType seleccionado.

Al continuar deben conservarse como mínimo:

- cropId;
- RecordType.

El User NO debe seleccionarse manualmente.

El `userId` debe obtenerse desde la sesión autenticada.

No mostrar `Guardar borrador` en esta pantalla porque todavía no existen datos
del formulario que guardar.

No necesita bottom navigation.

Existe una referencia visual para esta pantalla, pero debe adaptarse a estas
decisiones funcionales.

---

## 4. Registro de suelo

Debe crear un Record de tipo SOIL.

Debe quedar asociado a:

- Crop seleccionado;
- Farmer autenticado.

Campos basados en el modelo existente:

- pH;
- conductividad eléctrica;
- N;
- P;
- K;
- Ca;
- Mg;
- materia orgánica;
- observaciones.

Los campos numéricos deben validarse adecuadamente.

No asumir que todos los valores son obligatorios si el modelo permite valores
opcionales.

Al guardar se debe utilizar `RecordRepository` y la infraestructura existente.

El guardado del Record y sus datos específicos debe mantenerse consistente y
transaccional.

Después de guardar correctamente debe existir una respuesta visual clara y el
registro debe poder consultarse posteriormente desde Historial.

---

## 5. Registro de agua

Debe crear un Record de tipo WATER asociado al Crop y Farmer actual.

Campos:

- pH;
- conductividad eléctrica;
- N;
- P;
- K;
- Ca;
- Mg;
- fuente de agua;
- observaciones.

Debe utilizar la misma arquitectura de creación utilizada por SOIL.

---

## 6. Registro foliar

Debe crear un Record de tipo FOLIAR asociado al Crop y Farmer actual.

Campos:

- N;
- P;
- K;
- Ca;
- Mg;
- síntomas;
- observaciones;
- fotografías.

Las fotografías se almacenarán como archivos.

Room almacenará solamente sus rutas o URI.

Debe contemplarse posteriormente el uso de cámara y/o galería según los
requisitos del proyecto.

---

## 7. Historial

Debe mostrar los Record asociados al Farmer autenticado.

Cada elemento debería mostrar como mínimo:

- cultivo;
- tipo de registro;
- fecha;
- estado.

Debe permitir abrir el detalle.

Debe existir una forma clara de reconocer los diferentes tipos de registro sin
sobrecargar visualmente la pantalla.

La sección Historial reciente del Home reutiliza esta información.

---

## 8. Detalle de registro

Debe mostrar información completa del Record seleccionado.

Debe mostrar únicamente los datos correspondientes a su tipo:

SOIL → SoilData
WATER → WaterData
FOLIAR → FoliarData

No mezclar campos que pertenezcan a tipos distintos.

En el futuro también podrá mostrar:

- revisión del Advisor;
- observaciones del Advisor;
- estado de validación.

Los datos originales introducidos por Farmer no deben ser modificados por
Advisor durante una revisión.

---

## 9. Predios

Debe permitir consultar los predios disponibles para Farmer.

Debe quedar preparada para:

- seleccionar predio;
- agregar predio;
- editar información básica.

Un Farm puede contener varios Crop.

No duplicar información propia de Crop dentro de Farm.

---

## 10. Detalle de predio / cultivos

Debe mostrar información del Farm y los Crop asociados.

Desde esta pantalla se podrá:

- consultar un Crop;
- agregar un Crop;
- editar un Crop cuando corresponda.

La implementación debe reutilizar la misma información y repositories utilizados
por Home.

---

## 11. Detalle de cultivo

Debe mostrar información propia del Crop:

- imagen;
- nombre;
- variedad;
- superficie;
- etapa fenológica.

También puede ofrecer acceso a:

- registros del Crop;
- programa nutricional asociado.

Los datos nutricionales históricos deben obtenerse desde Record.

No duplicarlos como propiedades permanentes de Crop.

---

## 12. Programa

Para Farmer esta sección es principalmente de consulta.

Farmer podrá consultar los programas nutricionales asociados a sus Crop cuando
esa funcionalidad esté disponible.

La creación y gestión principal del programa corresponde al flujo Advisor.

La lógica futura debe basarse en el Módulo 3 de:

https://nutrixpert.netlify.app/

No inventar fórmulas agronómicas, dosis ni reglas que no estén respaldadas por
los requisitos del proyecto o por la referencia correspondiente.

---

# Futuro Advisor

Advisor se implementará separadamente en:

`ui/advisor`

Sus funciones principales serán:

- revisar registros;
- consultar registros revisados;
- validar u observar registros;
- crear programas nutricionales;
- consultar programas nutricionales ya creados.

Advisor no debe modificar directamente los datos originales introducidos por
Farmer.

Las revisiones deben mantenerse separadas de los Record originales cuando esa
funcionalidad sea implementada.

Los programas nutricionales estarán asociados a un Crop específico.

El flujo actual de trabajo se centra exclusivamente en Farmer.

No desarrollar Advisor salvo que sea necesario definir algo compartido y exista
una instrucción explícita.

---

# Backend futuro

Posteriormente:

Android
→ Retrofit
→ Spring Boot
→ PostgreSQL.

El backend será responsable de:

- autenticación;
- autorización;
- usuarios;
- datos remotos;
- sincronización;
- reglas de negocio que correspondan.

La aplicación Android nunca debe conectarse directamente a PostgreSQL.

La futura autenticación remota reemplazará la autenticación local.

Idealmente la UI y el flujo Farmer no deberían necesitar cambios importantes
cuando esto ocurra; debería cambiar principalmente la fuente de datos y la
implementación de autenticación.

Room continuará siendo utilizado para soporte offline cuando corresponda.

---

# Referencias visuales

Las imágenes disponibles del Home y Nuevo registro deben utilizarse como
referencia visual.

Características deseadas:

- interfaz limpia;
- predominio de los verdes definidos en el tema;
- tarjetas claras;
- esquinas redondeadas;
- jerarquía tipográfica clara;
- espacios suficientes;
- controles fáciles de usar;
- diseño adecuado para uso en terreno.

Las referencias NO deben copiarse literalmente cuando contradigan una decisión
funcional vigente.

Ejemplo:

La referencia de Nuevo registro contiene `Guardar borrador`, pero esa acción NO
debe aparecer en la pantalla inicial de selección.

Antes de crear una pantalla nueva, revisar las referencias visuales y las
pantallas existentes para mantener consistencia.
