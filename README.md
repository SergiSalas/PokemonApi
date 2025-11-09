# Pokémon API 🧩

## Descripción del proyecto

Este proyecto es una **API REST** desarrollada en **Spring Boot** que se conecta con la [PokéAPI](https://pokeapi.co/), sincronizando sus datos a una base de datos local para exponer endpoints optimizados.  

El objetivo es evitar realizar peticiones constantes a la API pública, mejorando el rendimiento y reduciendo la carga sobre la fuente externa.  
Dado que los datos de los Pokémon **no cambian frecuentemente**, se ha optado por un **servicio programado que actualiza la base de datos cada 12 horas**, además de permitir la sincronización manual mediante un endpoint.

---
## 🧠 Decisión de diseño

Se decidió implementar una **base de datos local** junto con un **servicio de sincronización programado** para optimizar el rendimiento y la eficiencia de la aplicación. Dado que los datos de la PokéAPI no cambian constantemente, no es necesario realizar consultas a la API pública en cada solicitud.

### Ventajas de este enfoque

- **Eficiencia:** las consultas a nuestra API son rápidas y con baja latencia.  
- **Reducción de carga externa:** se minimizan las llamadas continuas a la PokéAPI, evitando sobrecarga en servicios externos.  
- **Disponibilidad parcial offline:** los datos permanecen accesibles incluso si la PokéAPI experimenta fallos o caídas temporales.  

### Consideraciones

- Los datos pueden tener hasta **12 horas de antigüedad**, pero el endpoint **`/pokemon/sync`** permite forzar la actualización bajo demanda, manteniendo la información actualizada cuando sea necesario.

---

## 📡 Endpoints disponibles

La API expone los siguientes endpoints bajo el path base `/pokemon`:

| Método | Endpoint | Descripción | Parámetros | Respuesta |
|:--------|:----------|:-------------|:------------|:-----------|
| **GET** | `/highest` | Devuelve los **N Pokémon más altos**, ordenados por altura. | `numPokemon` *(int ≥ 1)* | `200 OK` → Lista `PokemonDto` |
| **GET** | `/heaviest` | Devuelve los **N Pokémon más pesados**, ordenados por peso. | `numPokemon` *(int ≥ 1)* | `200 OK` → Lista `PokemonDto` |
| **GET** | `/highestExperience` | Devuelve los **N Pokémon con mayor experiencia base**. | `numPokemon` *(int ≥ 1)* | `200 OK` → Lista `PokemonDto` |
| **POST** | `/sync` | Sincroniza la base de datos con la **PokéAPI externa**. | — | `204 No Content` |

**Códigos de error posibles:**  
`400 Bad Request` → Parámetro inválido (`numPokemon < 1`)  
`500 Internal Server Error` → Error interno o fallo en la sincronización  

**Ejemplos de uso:**
```bash
# Obtener los 10 Pokémon más altos
curl -s "http://localhost:8080/pokemon/highest?numPokemon=10"

# Obtener los 5 Pokémon más pesados
curl -s "http://localhost:8080/pokemon/heaviest?numPokemon=5"

# Sincronizar la base de datos
curl -X POST "http://localhost:8080/pokemon/sync"

```
## Arquitectura y estructura del proyecto

La aplicación sigue una arquitectura en capas típica de Spring Boot, separando responsabilidades para mantener el código limpio, escalable y fácil de testear.

### 🧩 Capa **Domain** — Entidad `Pokemon`

Esta clase representa la **entidad principal** del dominio, mapeada a la tabla `Pokemons` en la base de datos.

#### 📘 Descripción
La entidad `Pokemon` almacena los datos esenciales obtenidos desde la **PokéAPI**, junto con metadatos internos del sistema (como la fecha de sincronización o el JSON original).  
Forma parte de la capa **Domain**, encargada de modelar los objetos persistentes de la aplicación.

#### 🗂️ Estructura del modelo
| Campo | Tipo | Descripción |
|:-------|:------|:-------------|
| `id` | `String` | Identificador único generado automáticamente (UUID). |
| `pokeApiId` | `Integer` | ID oficial del Pokémon según la PokéAPI. |
| `name` | `String` | Nombre del Pokémon. |
| `weight` | `Integer` | Peso del Pokémon (en hectogramos). |
| `height` | `Integer` | Altura del Pokémon (en decímetros). |
| `baseExperience` | `Integer` | Experiencia base otorgada al derrotar al Pokémon. |
| `rawJson` | `String` *(Lob)* | Contenido JSON completo obtenido de la PokéAPI, almacenado para referencia o depuración. |
| `lastSynced` | `Instant` | Fecha y hora de la última sincronización con la API externa. |

#### ⚙️ Anotaciones clave
- `@Entity` y `@Table(name = "Pokemons")` → Define la entidad JPA y su tabla.  
- `@Id` → Marca el identificador primario.  
- `@Lob` → Permite almacenar el JSON completo sin límite de tamaño.  
- `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` (Lombok) → Generan getters, setters y constructores automáticamente.

#### 💡 Notas
- El `id` se genera con `UUID.randomUUID()` al instanciar el objeto.
- El campo `rawJson` permite conservar la respuesta original de la API.
- Esta entidad es utilizada por el repositorio JPA dentro de la capa **Repository**.


### 🗄️ Capa **Persistence** — Repositorio `PokemonRepository`

Esta interfaz define el **acceso a datos** para la entidad `Pokemon` utilizando **Spring Data JPA**. Forma parte de la capa **Persistence**, encargada de interactuar con la base de datos de manera abstracta y eficiente.

#### 📘 Descripción
`PokemonRepository` extiende `JpaRepository`, lo que permite realizar operaciones CRUD estándar sobre `Pokemon` sin necesidad de implementación manual. Además, incluye consultas personalizadas para obtener Pokémon según peso, altura o experiencia base.

#### 📝 Métodos principales

| Método | Descripción |
|:-------|:------------|
| `Optional<Pokemon> findByPokeApiId(Integer pokeapiId)` | Busca un Pokémon por su ID oficial en la PokéAPI. |
| `List<Pokemon> findTopPokemonByWeight(Pageable pageable)` | Devuelve los Pokémon más pesados, ordenados descendente por `weight`. |
| `List<Pokemon> findTopPokemonByHeight(Pageable pageable)` | Devuelve los Pokémon más altos, ordenados descendente por `height`. |
| `List<Pokemon> findTopPokemonByBaseExperience(Pageable pageable)` | Devuelve los Pokémon con mayor `baseExperience`, ordenados descendente. |

#### ⚙️ Anotaciones clave
- `@Repository` → Marca la interfaz como componente de Spring para persistencia de datos.
- `@Query` → Define consultas JPQL personalizadas para obtener listas ordenadas.
- `Pageable` → Permite limitar el número de resultados devueltos por cada consulta (paginación).

#### 💡 Notas
- Gracias a `JpaRepository`, también se heredan métodos como `save()`, `findAll()`, `delete()`, etc.
- Las consultas personalizadas facilitan implementar los endpoints `/highest`, `/heaviest` y `/highestExperience` de manera eficiente.

### ⚙️ Capa **Service** — Servicio `PokemonService`

Esta clase implementa la **lógica de negocio** de la aplicación y sirve como intermediario entre los controladores (`PokemonController`) y la capa de persistencia (`PokemonRepository`).  

Forma parte de la capa **Service**, que encapsula reglas de negocio y transforma entidades a DTOs para la exposición a la API.

#### 📘 Funcionalidades principales

| Método | Descripción |
|:-------|:------------|
| `getHeaviestPokemon(int numPokemon)` | Devuelve los N Pokémon más pesados, mapeados a `PokemonDto`. |
| `getHighestPokemon(int numPokemon)` | Devuelve los N Pokémon más altos, mapeados a `PokemonDto`. |
| `getHighestExperiencesPokemon(int numPokemon)` | Devuelve los N Pokémon con mayor experiencia base, mapeados a `PokemonDto`. |
| `syncDataBase()` | Invoca `PokemonSyncService` para sincronizar todos los Pokémon desde la PokéAPI externa. |

#### ⚙️ Detalles de implementación
- **Paginación:** Se utiliza `PageRequest.of(0, numPokemon)` para limitar los resultados a N Pokémon.
- **Transformación:** Se convierten entidades `Pokemon` a DTOs `PokemonDto` mediante `map`.
- **Manejo de errores:** Se capturan excepciones y se lanzan como `RuntimeException` para simplificar el control de errores en los controladores.
- **Dependencias:**  
  - `PokemonRepository` → Acceso a datos.  
  - `PokemonSyncService` → Sincronización con API externa.

#### 💡 Notas
- Esta capa asegura que los controladores no manipulen directamente la base de datos.
- Permite centralizar la lógica de filtrado, ordenación y conversión a DTO.
- Se puede extender fácilmente para agregar nuevos criterios de búsqueda o lógica adicional sin modificar el controlador.

### 🔄 Capa **Service** — Servicio `PokemonSyncService`

Este servicio se encarga de **sincronizar la base de datos** con la información completa de la **PokéAPI**. Forma parte de la capa **Service** y es utilizado internamente por `PokemonService`.

#### 📘 Funcionalidades principales

| Método | Descripción |
|:-------|:------------|
| `syncAllPokemons()` | Descarga hasta 1500 Pokémon desde la API externa y los guarda en la base de datos. Programado para ejecutarse automáticamente cada 12 horas mediante `@Scheduled`. |
| `fetchAndMapPokemon(PokemonBasic basicInfo)` | Obtiene el detalle completo de cada Pokémon y lo mapea a la entidad `Pokemon`. |

#### ⚙️ Detalles de implementación
- **API externa:** Se usa `RestClient` para consumir `https://pokeapi.co/api/v2/pokemon`.
- **Paginación:** Se limita a 1500 Pokémon con `?limit=1500`.
- **Mapeo:** Cada respuesta JSON se convierte a `PokemonDetailResponse` mediante `ObjectMapper` y luego a la entidad `Pokemon`.
- **Almacenamiento:** Se guardan todos los Pokémon sincronizados usando `pokemonRepository.saveAll(pokemons)`.
- **Cron Job:** La anotación `@Scheduled(cron = "0 0 */12 * * *")` permite sincronizar la base de datos automáticamente cada 12 horas.
- **Manejo de errores:** Si falla la obtención de un Pokémon se devuelve `null` y se filtra; si falla la lista completa, se lanza `RuntimeException`.

#### 💡 Notas
- Mantiene la base de datos local actualizada con la información más reciente de la PokéAPI.
- Permite que los servicios de consulta (`PokemonService`) trabajen con datos completos y consistentes.

### 🗃️ Capa **DTOs** — Objetos de transferencia de datos

Estos DTOs se utilizan para **intercambiar información** entre la capa de servicio y los controladores, así como para mapear las respuestas de la **PokéAPI** sin exponer la entidad `Pokemon` directamente.

---

#### 1️⃣ `PokemonDto`
Convierte un objeto `Pokemon` en su correspondiente DTO y se utiliza para las respuestas de la API.

| Campo | Tipo | Descripción |
|:------|:-----|:------------|
| `pokeApiId` | `Integer` | ID oficial del Pokémon en la PokéAPI. |
| `name` | `String` | Nombre del Pokémon. |
| `weight` | `Integer` | Peso del Pokémon (hectogramos). |
| `height` | `Integer` | Altura del Pokémon (decímetros). |
| `baseExperience` | `Integer` | Experiencia base del Pokémon. |

---

#### 2️⃣ `PokemonDetailResponse`
Mapea la respuesta detallada de la PokéAPI para un Pokémon individual.

| Campo | Tipo | Descripción |
|:------|:-----|:------------|
| `id` | `Integer` | ID del Pokémon en la PokéAPI. |
| `name` | `String` | Nombre del Pokémon. |
| `height` | `Integer` | Altura del Pokémon. |
| `weight` | `Integer` | Peso del Pokémon. |
| `base_experience` | `Integer` | Experiencia base del Pokémon. |

---

#### 3️⃣ `PokemonListResponse`
Mapea la respuesta de la lista de Pokémon obtenida desde la PokéAPI.

| Campo | Tipo | Descripción |
|:------|:-----|:------------|
| `count` | `Integer` | Número total de Pokémon disponibles en la API. |
| `next` | `String` | URL de la siguiente página (paginación). |
| `previous` | `String` | URL de la página anterior. |
| `results` | `List<PokemonBasic>` | Lista de Pokémon básicos (nombre + URL de detalle). |

**Clase interna `PokemonBasic`:**

| Campo | Tipo | Descripción |
|:------|:-----|:------------|
| `name` | `String` | Nombre del Pokémon. |
| `url` | `String` | URL de la PokéAPI para obtener detalles completos del Pokémon. |

---

#### 💡 Notas
- Los DTOs permiten **desacoplar** la capa de persistencia de la API REST.  
- `PokemonDto` se utiliza en los endpoints `/highest`, `/heaviest` y `/highestExperience`.  
- `PokemonDetailResponse` y `PokemonListResponse` se usan internamente en `PokemonSyncService` para mapear la información obtenida de la PokéAPI.

### ⚠️ Manejo global de excepciones — `GlobalExceptionHandler`

La aplicación cuenta con un **manejador global de excepciones** para centralizar la respuesta ante errores y validar parámetros de forma uniforme. Se encuentra en la capa de **web/exceptionHandler**.

#### 📘 Funcionalidades principales

| Excepción | Manejo | Respuesta HTTP | Mensaje |
|:-----------|:------|:---------------|:--------|
| `ConstraintViolationException` | Parámetros inválidos (ej. `numPokemon < 1`) | `400 Bad Request` | `{"error": "Invalid parameter"}` |
| `RuntimeException` | Errores generales | `400 Bad Request` si la causa es `IllegalArgumentException` <br> `500 Internal Server Error` para otros casos | Mensaje de la excepción o `"Internal server error"` |

#### ⚙️ Detalles de implementación
- `@ControllerAdvice` → Permite interceptar excepciones de cualquier controlador.
- `@ExceptionHandler` → Define métodos específicos para distintos tipos de excepción.
- Devuelve un `ResponseEntity<Map<String,String>>` con un mensaje uniforme en JSON.
- Mejora la **experiencia del API** al estandarizar errores y códigos HTTP.

#### 💡 Notas
- Garantiza que los endpoints no expongan trazas de stack ni detalles internos al cliente.
- Simplifica el manejo de errores en controladores, delegando la lógica de validación y respuesta a un único componente.

### 🌐 Capa **Web / Controller** — `PokemonController`

El controlador `PokemonController` expone los endpoints de la API REST para consultar y sincronizar Pokémon. Forma parte de la capa **Web**, encargada de recibir peticiones HTTP, validar parámetros y delegar la lógica al servicio correspondiente.

#### 📘 Endpoints principales

| Método | Endpoint | Descripción | Parámetros | Respuesta |
|:--------|:----------|:-------------|:------------|:-----------|
| **GET** | `/highest` | Devuelve los N Pokémon más altos | `numPokemon` (int ≥ 1) | `200 OK` → Lista `PokemonDto` |
| **GET** | `/heaviest` | Devuelve los N Pokémon más pesados | `numPokemon` (int ≥ 1) | `200 OK` → Lista `PokemonDto` |
| **GET** | `/highestExperience` | Devuelve los N Pokémon con mayor experiencia base | `numPokemon` (int ≥ 1) | `200 OK` → Lista `PokemonDto` |
| **POST** | `/sync` | Sincroniza la base de datos con la PokéAPI | — | `204 No Content` |

#### ⚙️ Detalles de implementación
- **Validación de parámetros:** `@Min(1)` asegura que `numPokemon` sea ≥ 1.
- **Documentación automática:** `@Operation`, `@ApiResponses` y `@Tag` integran OpenAPI/Swagger para generar la documentación de la API.
- **Delegación al servicio:** Cada endpoint llama a métodos de `PokemonService` para obtener o sincronizar datos.
- **Manejo de respuestas:** Se utiliza `ResponseEntity` para controlar códigos HTTP y devolver JSON de manera consistente.

#### 💡 Notas
- Los endpoints `GET` (`/highest`, `/heaviest`, `/highestExperience`) devuelven listas de `PokemonDto`, manteniendo la capa de persistencia encapsulada.  
- El endpoint `POST /sync` permite actualizar la base de datos con los datos más recientes de la PokéAPI sin exponer la lógica interna.  
- La documentación Swagger permite probar los endpoints directamente desde la interfaz web si `springdoc-openapi` está configurado.

## 🧪 Pruebas del Proyecto

El proyecto cuenta con **tests unitarios** y **tests de integración** para garantizar el correcto funcionamiento de la API y de la sincronización con la PokéAPI.

---

### 1️⃣ Tests de Integración — `PokemonIntegrationTest`

- **Objetivo:** Verificar que los endpoints REST funcionan correctamente con la base de datos.
- **Cobertura:**
  - Endpoints `/highest`, `/heaviest`, `/highestExperience`.
  - Endpoint `/sync`.
  - Manejo de errores cuando se pasan parámetros inválidos.
- **Herramientas:** `SpringBootTest`, `MockMvc`.
- **Ejemplo de verificación:**  
  Se comprueba que `/pokemon/highest?numPokemon=3` devuelve los 3 Pokémon más altos en orden descendente.

---

### 2️⃣ Tests Unitarios — `PokemonServiceTest`

- **Objetivo:** Validar la lógica de negocio en `PokemonService`.
- **Cobertura:**
  - Métodos: `getHeaviestPokemon`, `getHighestPokemon`, `getHighestExperiencesPokemon`, `syncDataBase`.
  - Manejo de excepciones lanzadas por el repositorio o el servicio de sincronización.
- **Herramientas:** `Mockito`, `JUnit 5`.
- **Ejemplo de verificación:**  
  Se simula que el repositorio devuelve una lista de Pokémon y se comprueba que el servicio transforma correctamente los objetos en `PokemonDto`.

---

### 3️⃣ Tests Unitarios — `PokemonSyncServiceTest`

- **Objetivo:** Comprobar la sincronización con la PokéAPI.
- **Cobertura:**
  - Método `syncAllPokemons`.
  - Manejo de errores al obtener la lista de Pokémon o los detalles individuales.
  - Guardado correcto de Pokémon en la base de datos.
- **Herramientas:** `Mockito`, `JUnit 5`.
- **Notas:**
  - Se simula la respuesta de la PokéAPI con `RestClient` y `ObjectMapper`.
  - Se verifica que la base de datos solo se actualiza con Pokémon válidos.

---

### 4️⃣ Tests Unitarios — `PokemonControllerTest`

- **Objetivo:** Verificar el comportamiento del controlador REST.
- **Cobertura:**
  - Endpoints `/highest`, `/heaviest`, `/highestExperience` y `/sync`.
  - Respuestas HTTP correctas (`200 OK`, `204 No Content`) y contenido JSON.
- **Herramientas:** `Mockito`, `JUnit 5`, `MockMvc`.

---

### 5️⃣ Tests Unitarios — `GlobalExceptionHandlerTest`

- **Objetivo:** Validar el manejo global de excepciones.
- **Cobertura:**
  - `RuntimeException` con causa `IllegalArgumentException` → `400 Bad Request`.
  - `RuntimeException` genérica → `500 Internal Server Error`.
- **Notas:** Asegura que los errores de validación y fallos internos se manejan de forma consistente para los clientes de la API.

---

💡 **Resumen:**  
El proyecto combina **tests de integración** para validar el flujo completo de la API con la base de datos y **tests unitarios** para comprobar la lógica interna de los servicios y el manejo de errores, garantizando alta confiabilidad y facilidad de mantenimiento.

## 💡 Posibles mejoras

- **Excepciones personalizadas:** crear clases de error específicas para manejar distintos tipos de fallos de manera más clara y controlada.  
- **Más atributos de Pokémon:** incluir información adicional como tipos, habilidades, sprites o movimientos.  
- **Logging avanzado:** implementar registros detallados de las operaciones para facilitar debugging y monitorización.  
- **Base de datos persistente:** reemplazar H2 por una base de datos real (PostgreSQL, MySQL, etc.) para entornos de producción y escalabilidad.


📸 Capturas

1️⃣ Documentación Swagger

<img width="1856" height="1040" alt="image" src="https://github.com/user-attachments/assets/7354377d-2c02-47e3-846b-c3dee91730cb" />

2️⃣ Ejemplo de petición GET /highest

<img width="1132" height="1076" alt="image" src="https://github.com/user-attachments/assets/b7db256c-cebc-410b-80be-25f1f8c4febc" />

3️⃣ Ejemplo de petición GET /heaviest

<img width="1132" height="1067" alt="image" src="https://github.com/user-attachments/assets/d10b0235-45c2-40c3-9c1c-afc9faff12f7" />

4️⃣ Ejemplo de petición GET /highestExperience

<img width="1131" height="1032" alt="image" src="https://github.com/user-attachments/assets/3e7fce47-6a76-41f5-89e8-40acbbe8c957" />

5️⃣ Ejemplo de sincronización POST /sync

<img width="1100" height="287" alt="image" src="https://github.com/user-attachments/assets/65b50a46-4fe5-4120-9209-32e3a7c29d15" />

6️⃣ Archivo de colección Postman

[Descargar colección Postman](postman/PokemonAPI.postman_collection.json)

Incluye todos los endpoints listos para probar en Postman.
