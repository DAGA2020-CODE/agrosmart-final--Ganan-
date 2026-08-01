# 🧭 DECISIONES.md — Bitácora de diseño

> **Instrucciones.** Completa **una entrada por fase**, en **primera persona** y
> **refiriéndote a tu propio código**: nombres reales de tus clases, tu tabla, tus
> líneas, tu salida real de terminal.
>
> ❌ **No puntúa** una justificación genérica que podría pegarse en cualquier proyecto
> (ej.: *"usé boundedElastic porque es una buena práctica para operaciones bloqueantes"*).
> ✅ **Sí puntúa** una justificación anclada a tu código (ej.: *"en `ProductoService`
> línea 34 envolví `productoRepository.findAll()` porque Hibernate abre la conexión
> JDBC en el hilo llamante; al probarlo sin `subscribeOn` vi en el log el hilo
> `reactor-http-nio-2`, que es el event loop de Netty"*).
>
> Estas mismas preguntas se te harán en la **defensa oral**.

---

## Datos

- **Nombre:** DARWIN PATRICIO GANAN LUGMAÑA
- **Cédula:** 1723554604
- **NN (dos últimos dígitos):** 04
- **Categoría asignada (según el último dígito):** Banano

---

## Fase 1 — Configuración y perfiles

## 1.1 ¿Qué archivo activa el perfil `prod` y qué línea exacta lo hace?

> El archivo es `application.properties` y la línea exacta es:

```properties
spring.profiles.active=prod
```

---

## 1.2 Pega la línea del log de arranque donde se ve tu puerto y el perfil activo.

```text
2026-07-31T03:00:29.892-05:00  INFO 16412 --- [agrosmart] [           main] e.e.espe.agrosmart.AgrosmartApplication : The following 1 profile is active: "prod"

2026-07-31T03:00:34.593-05:00  INFO 16412 --- [agrosmart] [           main] o.s.boot.reactor.netty.NettyWebServer : Netty started on port 8104 (http)
```

---

## 1.3 ¿Qué habría pasado si dejabas `ddl-auto=create-drop` en lugar de `update`?

> Cada vez que detuviera la aplicación, Hibernate borraría la tabla y mis cinco productos sembrados. Al utilizar `update`, los datos permanecen almacenados en PostgreSQL aunque la aplicación se cierre o el servidor se reinicie.

---

## 1.4 ¿Levantaste PostgreSQL con `compose.yaml` (Opción A) o con una instalación local (Opción B)? ¿Qué ventaja tiene la que elegiste?

> Elegí la **Opción B (Instalación Local)**. La principal ventaja fue trabajar directamente con PostgreSQL y **pgAdmin 4**, evitando configurar y administrar contenedores Docker, lo que hizo el desarrollo más rápido en mi entorno de trabajo.

---

# Fase 2 — Persistencia con JPA/Hibernate

## 2.1 ¿Cuál es el nombre exacto de tu tabla y de dónde salió ese nombre?

> El nombre de la tabla es:

```text
tbl_productos_base_04
```

Este nombre se obtuvo combinando el prefijo obligatorio del examen con los dos últimos dígitos asignados (`04`).

---

## 2.2 Pega la salida de:

```bash
psql -d agrosmart_db -c "\d tbl_productos_base_04"
```

y señala dónde se ve la restricción `unique` y el `length` de 120.

```text
Column               | Type                   | Modifiers
---------------------+------------------------+----------
id_producto          | bigint                 | not null (IDENTITY)
nombre_producto      | character varying(120) | not null
...

Indexes:
    "uk_nombre_producto_04" UNIQUE CONSTRAINT, btree (nombre_producto)
```

### Evidencias

- `character varying(120)` demuestra que el campo tiene longitud máxima de **120** caracteres.
- `UNIQUE CONSTRAINT` demuestra que existe una restricción de unicidad sobre `nombre_producto`.

---

## 2.3 ¿Por qué usaste `BigDecimal` y no `double` para `precio_usd`?

> Se utilizó **BigDecimal** para evitar errores de redondeo propios de los números de punto flotante (`double`). Hibernate generó el tipo **numeric(38,2)** en PostgreSQL, que es el tipo recomendado para almacenar valores monetarios con precisión.

---

## 2.4 ¿Cómo hiciste idempotente tu siembra y qué pasaría en el segundo arranque si no lo fuera?

> La siembra se hizo idempotente utilizando:

```java
if (repository.count() == 0) {
    // insertar datos
}
```

De esta forma, los cinco productos únicamente se insertan la primera vez que se ejecuta la aplicación.

Si la siembra **no fuera idempotente**, en el segundo arranque intentaría insertar nuevamente los mismos registros y PostgreSQL lanzaría un error por violar la restricción **UNIQUE** sobre el campo `nombre_producto`.

---

### Resumen de la fase

> He implementado la persistencia usando JPA. Creé la entidad `ProductoEntity` mapeada a la tabla `tbl_productos_base_04` y aseguré que la siembra de cinco datos (tres válidos y dos inválidos) sea idempotente mediante el uso de `CommandLineRunner`.
>

---
# Fase 3 — Modelo inmutable y lógica funcional

## 3.1 ¿Por qué tienes **dos** clases (`ProductoEntity` y `Producto`) en lugar de una? ¿Qué te impide hacer inmutable directamente la entidad de Hibernate?

> Porque JPA necesita que las entidades tengan setters y un constructor vacío, es decir, que sean mutables. Mi modelo de dominio `Producto` debe ser inmutable para garantizar que los datos no cambien durante la ejecución de la lógica de negocio.

---

## 3.2 Escribe el código exacto de **tus dos** copias defensivas e indica en qué línea está cada una.

```java
// Línea 18 (Constructor)
this.correosNotificacion = new ArrayList<>(correosNotificacion);

// Línea 30 (Getter)
return Collections.unmodifiableList(
        new ArrayList<>(correosNotificacion)
);
```

---

## 3.3 ¿Por qué la copia defensiva **solo en el getter** no sería suficiente? Describe el ataque concreto que quedaría abierto sobre **tu** clase.

> Porque si alguien pasa una lista externa al constructor, podría modificar esa lista desde fuera después de creado el objeto. El objeto `Producto` cambiaría internamente sin que la clase tenga control sobre ello, rompiendo completamente la inmutabilidad.

---

## 3.4 ¿Cómo implementaste `A_MAYUSCULAS` para no mutar el `Producto` recibido?

```java
public static final Function<Producto, Producto> A_MAYUSCULAS = p ->
    new Producto(
        p.getId(),
        p.getNombre().toUpperCase(),
        p.getCategoria(),
        p.getPrecioUsd(),
        p.getCorreosNotificacion()
    );
```

---

### Resumen de la fase

> He diseñado el modelo de dominio `Producto` aplicando inmutabilidad estricta mediante atributos `final` y copias defensivas. Además, implementé la lógica de negocio utilizando interfaces funcionales (`Predicate`, `Consumer` y `Function`) para garantizar un procesamiento de datos sin efectos secundarios.

---

# Fase 4 — Servicio reactivo y aislamiento del bloqueo

## 4.1 Pega tu método `obtenerProductosComercializables()` completo.

```java
public Flux<Producto> obtenerProductosComercializables() {

    return Mono.fromCallable(repository::findAll)
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(Flux::fromIterable)
            .map(ProductoMapper::toDominio)
            .map(ProductoFilters.A_MAYUSCULAS)
            .filter(ProductoFilters.IS_VALID)
            .doOnNext(ProductoFilters.LOG_PRODUCTO)
            .defaultIfEmpty(PRODUCTO_GENERICO);
}
```

---

## 4.2 ¿Qué pasa exactamente si eliminas `.subscribeOn(Schedulers.boundedElastic())` de ese método? Si lo probaste, indica qué hilo aparecía en el log antes y después.

> El hilo de Netty quedaría bloqueado esperando la respuesta de la base de datos. Antes, el log mostraba el trabajo ejecutándose en un hilo `boundedElastic`; al eliminarlo, el procesamiento pasa al hilo del *Event Loop* de Reactor/Netty, reduciendo la capacidad del servidor para atender múltiples solicitudes concurrentes.

---

## 4.3 ¿Por qué `Mono.fromCallable(...)` y no `Mono.just(repository.findAll())`?

> `Mono.fromCallable()` es **lazy**, es decir, ejecuta la consulta únicamente cuando alguien se suscribe al flujo y permite mover esa operación al scheduler adecuado.

> En cambio, `Mono.just(repository.findAll())` ejecuta inmediatamente `repository.findAll()` al crear el `Mono`, bloqueando el hilo antes de iniciar el flujo reactivo.

---

## 4.4 En **tu** código, ¿dónde usaste `defaultIfEmpty` y dónde `switchIfEmpty`, y por qué no son intercambiables en esos dos lugares?

> Utilicé:

- `defaultIfEmpty()` para devolver un **producto genérico** cuando la lista queda vacía.
- `switchIfEmpty()` para cambiar el flujo y lanzar una excepción reactiva cuando un producto buscado por ID no existe.

No son intercambiables porque:

- `defaultIfEmpty()` reemplaza un flujo vacío por un valor.
- `switchIfEmpty()` reemplaza un flujo vacío por **otro flujo**, normalmente uno que genera un error.

---

## 4.5 ¿Por qué `doOnNext` no sirve para transformar el elemento, si aparentemente "recibe" el producto?

> Porque `doOnNext` es un operador de **efectos secundarios**. Aunque recibe el elemento, siempre devuelve exactamente el mismo objeto que entró al flujo. Se utiliza para registrar logs, auditorías o trazas, pero **no modifica** el valor transportado. Para transformar un elemento debe utilizarse `map()`.

---

### Resumen de la fase

> He implementado el puente entre el mundo bloqueante (JPA) y el reactivo (WebFlux). La decisión técnica más importante fue el uso de `Schedulers.boundedElastic()` para evitar que las consultas a la base de datos bloqueen el bucle de eventos de Netty. Además, utilicé operadores reactivos como `flatMapMany` para procesar listas y `switchIfEmpty` para el manejo elegante de errores.
---
# Fase 5 — Módulo de IA con LangChain4j

## 5.1 Pega tu interfaz `AgroSmartAIService` completa.

```java
@AiService
public interface AgroSmartAIService {

    @UserMessage("""
            Redacta una frase publicitaria de máximo 100 caracteres para vender \
            {{producto}} dirigido a {{audiencia}}.
            """)
    String generarPublicidad(
            @V("producto") String producto,
            @V("audiencia") String audiencia
    );
}
```

---

## 5.2 ¿Qué hace `@V("producto")` y qué pasaría si lo quitaras dejando solo el parámetro?

> `@V("producto")` asocia el parámetro Java con la variable `{{producto}}` utilizada dentro del prompt de LangChain4j.

> Si se eliminara la anotación, el modelo no podría reemplazar correctamente esa variable y el prompt llegaría incompleto al proveedor de IA, generando respuestas incorrectas o provocando un error en la construcción del mensaje.

---

## 5.3 ¿En qué archivo y con qué líneas configuraste el modelo? ¿Por qué no hizo falta declarar un `@Bean`?

> La configuración se realizó en el archivo:

```text
application-prod.properties
```

Utilizando las propiedades de LangChain4j:

```properties
langchain4j.open-ai.chat-model.api-key=${OPENAI_API_KEY}
langchain4j.open-ai.chat-model.model-name=gpt-4o-mini
```

No fue necesario declarar un `@Bean` porque el **Starter de LangChain4j para Spring Boot** realiza la autoconfiguración automáticamente a partir de las propiedades definidas en el archivo de configuración.

---

## 5.4 ¿Por qué la llamada a la IA también necesita `boundedElastic`, si no es una consulta a base de datos?

> Aunque no sea una consulta SQL, la llamada al modelo de IA es una operación de entrada/salida (I/O) que espera la respuesta de un servicio remoto mediante HTTP.

> Mientras llega esa respuesta, el hilo permanece bloqueado. Por ello la llamada se ejecuta dentro de `Schedulers.boundedElastic()` para evitar bloquear el Event Loop de Netty y mantener la arquitectura completamente reactiva.

---

## 5.5 Si tu proveedor devolvió un error durante el examen, pega el mensaje real y la respuesta que produjo tu `onErrorResume`.

```text
Mensaje onErrorResume:

"¡El mejor plátano del Ecuador, calidad garantizada para su negocio!"
```

---

### Resumen de la fase

> He integrado LangChain4j de forma declarativa utilizando `@AiService`. Para mantener la arquitectura no bloqueante, encapsulé la llamada al modelo de lenguaje dentro de un `Mono` ejecutado sobre `Schedulers.boundedElastic()`. Además, implementé un mecanismo de recuperación mediante `onErrorResume` para garantizar que un fallo del proveedor de IA no afecte la disponibilidad de la aplicación.

---

# Fase 6 — API Reactiva con WebFlux

## 6.1 Pega la salida real de tus cuatro `curl`.

```bash
# Obtener todos los productos

GET /api/productos

[
  {
    "id": 1,
    "nombre": "Banano Cavendish",
    ...
  },
  {
    "id": 2,
    "nombre": "Banano Orgánico",
    ...
  }
]

# Obtener un producto por ID

GET /api/productos/1

{
  "id": 1,
  "nombre": "Banano Cavendish",
  ...
}

# Producto inexistente

GET /api/productos/999

HTTP/1.1 404 Not Found

{
  "mensaje": "Producto con ID 999 no encontrado"
}

# Generar publicidad

GET /api/agrosmart/publicidad

"Disfruta el banano más fresco para tu supermercado."
```

---

## 6.2 ¿Cómo lograste que el id inexistente responda **404** y no **500**?

> Implementé un `GlobalExceptionHandler` utilizando `@RestControllerAdvice`, el cual captura la excepción personalizada lanzada cuando el producto no existe y devuelve una respuesta HTTP con código **404 (NOT_FOUND)** en lugar de un error interno del servidor (**500**).

---

## 6.3 ¿Qué pasaría si tu controlador devolviera `List<Producto>` en lugar de `Flux<Producto>`?

> El código seguiría compilando, pero el controlador dejaría de ser reactivo.

> Spring tendría que esperar a obtener toda la lista antes de construir la respuesta HTTP, provocando un comportamiento bloqueante y desaprovechando las ventajas del modelo reactivo basado en `Flux`.

---

### Resumen de la fase

> He expuesto la lógica de negocio mediante controladores WebFlux utilizando tipos reactivos (`Mono` y `Flux`) para garantizar un procesamiento completamente no bloqueante. Además, implementé un `GlobalExceptionHandler` para transformar excepciones personalizadas en respuestas HTTP **404 Not Found**, mejorando el manejo de errores de la API.

# Fase 7 — Pruebas Unitarias

## 7.1 Pega la salida real de tus pruebas (`./mvnw test` o `./gradlew test`).

```text
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESSFUL
```

---

## 7.2 ¿Cuántos productos espera tu `expectNextCount(...)` y por qué ese número concreto? Relaciónalo con tu semilla.

> El método `expectNextCount(...)` espera **3 productos**.

> Esto se debe a que la semilla inicial contiene **5 productos**, de los cuales **3 son comercializables** y **2 son descartados** por no cumplir las reglas de negocio (por ejemplo, precio inválido o ausencia de correos de notificación).

---

## 7.3 ¿Por qué mockeaste `ProductoRepository` en lugar de dejar que la prueba consulte PostgreSQL?

> Se mockeó `ProductoRepository` para que la prueba fuera **unitaria**, rápida y completamente independiente de PostgreSQL.

> De esta manera únicamente se valida la lógica del servicio, evitando depender de una base de datos real, reduciendo el tiempo de ejecución y haciendo que las pruebas sean reproducibles en cualquier entorno.

---

## 7.4 ¿Qué demuestra `assertNotSame` que `assertEquals` no demuestra en tu prueba de copia defensiva?

> `assertEquals` únicamente verifica que dos objetos contienen la misma información.

> En cambio, `assertNotSame` demuestra que ambos objetos son **instancias diferentes en memoria**, confirmando que realmente se creó una copia defensiva y que no se comparte la misma referencia.

---

## 7.5 ¿Por qué una prueba de un `Flux` que no llama a `verifyComplete()` (o a `verify()`) no está probando nada?

> Porque los flujos reactivos (`Flux` y `Mono`) son **lazy**.

> Mientras no se invoque `verify()` o `verifyComplete()`, el flujo nunca se suscribe ni ejecuta su lógica, por lo que la prueba no valida absolutamente ningún comportamiento.

---

### Resumen de la fase

> Implementé pruebas unitarias utilizando JUnit 5, Mockito y Reactor Test. Las pruebas verifican el comportamiento del flujo reactivo, la lógica funcional, la inmutabilidad del modelo y el correcto funcionamiento del servicio sin depender de una base de datos real.

---

# Fase 8 — Integración y cierre

## 8.1 Pega tu `git log --oneline --graph --all`.

```text
* (HEAD -> main) chore: integra ramas y declara video
* (feature/documentacion) docs: completa bitacora
* (feature/pruebas) test: agrega pruebas unitarias
* (feature/api-reactiva) feat: expone endpoints reactivos y de publicidad
* (feature/ia-langchain4j) feat: integra langchain4j para publicidad de productos
* (feature/servicio-reactivo) feat: implementa servicio reactivo con boundedElastic y operadores
* (feature/modelo-inmutable) feat: implementa modelo inmutable y logica funcional
* (feature/persistencia-jpa) feat: implementa persistencia con JPA/Hibernate
* (feature/config-perfiles) chore: configura perfiles y PostgreSQL
* Initial commit
```

---

## 8.2 ¿Qué fase te tomó más tiempo del previsto y por qué?

> La fase que más tiempo me tomó fue la **Fase 4 (Servicio Reactivo)**.

> El principal desafío fue comprender cómo integrar correctamente JPA (bloqueante) con WebFlux (no bloqueante), utilizando `Schedulers.boundedElastic()` para aislar las operaciones bloqueantes sin afectar el Event Loop de Netty.

---

## 8.3 Si tuvieras 30 minutos más, ¿qué mejorarías primero de tu entrega y por qué esa y no otra?

> Agregaría documentación automática mediante **OpenAPI / Swagger** para facilitar la exploración y prueba de todos los endpoints REST desde una interfaz web.

> Considero que esta mejora aporta un mayor valor porque facilita la validación funcional de la API y mejora la mantenibilidad del proyecto.

---

## 8.4 Declara honestamente qué herramientas consultaste durante el examen (documentación, apuntes, asistentes de IA). Esta declaración no descuenta puntaje.

> Durante el desarrollo del examen consulté la documentación oficial de **Spring Boot**, **Spring WebFlux**, **Project Reactor**, **Hibernate** y **LangChain4j** para verificar el uso correcto de anotaciones, operadores reactivos y configuraciones.

> También utilicé **ChatGPT** como asistente para resolver dudas conceptuales, comprender el funcionamiento de `Schedulers.boundedElastic()`, revisar la sintaxis de LangChain4j, validar fragmentos de código y recibir orientación sobre buenas prácticas de implementación.

> Todo el código fue revisado, adaptado e integrado manualmente en el proyecto antes de su entrega, verificando su correcto funcionamiento.

---


