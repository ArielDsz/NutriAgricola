# Instrucciones para Codex

Antes de modificar cualquier archivo:

1. Lee completamente `PROJECT_CONTEXT.md`.
2. Revisa el código existente relacionado con la tarea.
3. Revisa los documentos de `/docs` cuando sean relevantes para la funcionalidad.
4. Revisa las referencias visuales disponibles cuando se trabaje sobre UI.
5. Confirma que comprendes el estado actual antes de realizar cambios importantes.

El código existente es la principal referencia técnica y `PROJECT_CONTEXT.md`
define las decisiones funcionales vigentes.

Si ambos parecen contradecirse, no asumir cuál debe reemplazar al otro:
indicar la contradicción antes de realizar un cambio estructural.

---

# Alcance actual

El trabajo actual se centra exclusivamente en completar correctamente el flujo
Farmer.

No modificar ni desarrollar `ui/advisor` salvo instrucción explícita.

La arquitectura debe quedar preparada para integrar posteriormente:

- Advisor;
- backend Spring Boot;
- PostgreSQL;
- Retrofit;
- autenticación remota;
- sincronización.

No implementar estas tecnologías todavía salvo que se solicite expresamente.

---

# Reglas generales

- Código, clases, variables, funciones, archivos y paquetes en inglés.
- Texto visible en pantalla en español.
- Mantener separación entre `ui/farmer` y `ui/advisor`.
- Compartir solamente código realmente común.
- Utilizar Material 3.
- Utilizar el tema existente en `ui/theme`.
- No crear colores o tipografías arbitrarias.
- Mantener consistencia visual con las pantallas existentes y referencias.
- No duplicar lógica, modelos o estructuras existentes.
- No introducir librerías sin una necesidad clara.
- No refactorizar partes ajenas a la tarea únicamente por preferencia personal.
- No inventar requisitos.
- No inventar lógica agronómica.

---

# Arquitectura

Antes de crear una clase nueva, comprobar si ya existe una responsabilidad
equivalente.

Respetar la separación existente entre:

- domain;
- data;
- local;
- repository;
- mapper;
- ViewModel;
- UI.

No utilizar Entity directamente desde la UI.

No acceder directamente a DAO desde una Screen.

Las Screens deben centrarse en representar estado y eventos de interfaz.

La lógica y acceso a datos deben quedar en las capas correspondientes.

Utilizar los repositories, mappers, DAO y modelos existentes siempre que sea
posible.

No cambiar:

- modelos de dominio;
- entidades Room;
- relaciones;
- arquitectura;
- navegación general;

sin explicar primero por qué el cambio es necesario.

---

# Autenticación

La primera versión tendrá autenticación local con correo y contraseña.

No guardar la contraseña dentro del modelo `User`.

No guardar contraseñas en texto plano.

Las credenciales locales deben estar separadas del modelo principal de usuario
y asociarse mediante `userId`.

La solución debe diseñarse para que la autenticación local pueda ser sustituida
posteriormente por autenticación mediante backend sin rehacer el flujo Farmer.

La aplicación debe mantener un usuario autenticado durante la sesión.

El resto de la aplicación debe obtener el usuario actual desde esa sesión, no
mediante emails, ids o nombres hardcodeados.

El `User.id` autenticado debe utilizarse cuando una entidad requiera conocer qué
usuario realizó una acción.

No implementar todavía tokens, refresh tokens ni autenticación remota salvo que
se solicite.

---

# Datos y Room

Room es actualmente la fuente de datos local.

Mantener integridad entre:

Farm
→ Crop
→ Record
→ datos específicos del Record.

Cada Record también debe conservar la relación correspondiente con User.

Cuando una operación deba guardar varias entidades relacionadas y pueda quedar
inconsistente si falla parcialmente, utilizar una transacción.

Las imágenes deben almacenarse como archivos.

Room debe guardar rutas o URI, no BLOB de imágenes.

No hardcodear datos de demostración directamente dentro de Screens.

Cuando sean necesarios datos de prueba, utilizar la infraestructura existente,
preferentemente `DatabaseSeeder`.

---

# UI

Antes de implementar una pantalla nueva:

1. revisar su descripción en `PROJECT_CONTEXT.md`;
2. revisar las referencias visuales disponibles;
3. revisar componentes existentes reutilizables.

Las referencias visuales son orientación de estilo, no especificaciones
literales.

Si una imagen contradice `PROJECT_CONTEXT.md`, prevalece la decisión funcional
documentada.

Mantener:

- jerarquía visual clara;
- tamaños cómodos;
- espaciado consistente;
- componentes simples;
- interfaces utilizables en terreno.

No sacrificar funcionamiento correcto por intentar perfeccionar visualmente una
pantalla demasiado pronto.

---

# Usuario actual

Cuando exista un User autenticado, utilizar sus datos reales en la UI.

No mantener valores como iniciales, nombres o ids hardcodeados.

El círculo de cuenta del Home debe calcular sus iniciales desde:

- `firstName`;
- `lastName`.

Posteriormente ese elemento podrá abrir opciones de cuenta.

---

# Forma de trabajo

## Antes de un cambio importante

Indicar brevemente:

- qué se va a implementar;
- qué archivos se crearán;
- qué archivos se modificarán;
- si existe alguna decisión arquitectónica nueva;
- cualquier duda que necesite resolución.

Esperar confirmación cuando el cambio:

- afecte arquitectura;
- cambie modelos;
- cambie entidades Room;
- cambie navegación general;
- agregue una dependencia;
- cambie una decisión definida en `PROJECT_CONTEXT.md`.

Para cambios pequeños ya acordados puede continuar sin pedir confirmaciones
innecesarias.

---

## Durante la implementación

- Trabajar únicamente sobre la tarea solicitada.
- Mantener los cambios acotados.
- No modificar Advisor.
- No limpiar o reescribir archivos sin relación con la tarea.
- Reutilizar código existente.
- Mantener nombres y convenciones del proyecto.
- Evitar soluciones temporales que después deban eliminarse si existe una
  alternativa sencilla compatible con el diseño futuro.

---

## Al terminar

Indicar brevemente:

1. archivos creados;
2. archivos modificados;
3. lógica implementada;
4. decisiones tomadas;
5. problemas o pendientes encontrados.

Cuando sea posible:

- compilar el proyecto;
- ejecutar las comprobaciones pertinentes;
- informar claramente si existe algún error.

No ocultar errores de compilación ni asumir que algo funciona sin comprobarlo.

---

# Regla principal

Si falta información necesaria para tomar una decisión funcional o
arquitectónica correcta, preguntar antes de inventarla.

Si existen varias soluciones razonables con consecuencias distintas para el
proyecto, explicarlas brevemente antes de elegir una.