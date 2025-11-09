# Pokémon API 🧩

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.9.2-orange)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-lightgrey)
![H2](https://img.shields.io/badge/Database-H2-lightblue)
![Lombok](https://img.shields.io/badge/Lombok-1.18.30-blueviolet)
![Swagger](https://img.shields.io/badge/Swagger-2.7.0-yellowgreen)
![Status](https://img.shields.io/badge/status-Active-brightgreen)

## 🌎 Enlace a la versión en inglés
[English README](README_EN.md)

## Project Description

This project is a **REST API** developed with **Spring Boot** that connects to the [PokéAPI](https://pokeapi.co/), syncing its data to a local database to expose optimized endpoints.  

The goal is to avoid making constant requests to the public API, improving performance and reducing the load on the external source.  
Since Pokémon data **does not change frequently**, a **scheduled service updates the database every 12 hours**, while also allowing manual synchronization via an endpoint.

---

## 🧠 Design Decisions

A **local database** was implemented together with a **scheduled synchronization service** to optimize application performance and efficiency. Since PokéAPI data does not change constantly, it is unnecessary to query the public API for every request.

### Advantages of this approach

- **Efficiency:** API queries are fast and low-latency.  
- **Reduced external load:** Continuous calls to the PokéAPI are minimized, avoiding overloading external services.  
- **Partial offline availability:** Data remains accessible even if PokéAPI experiences temporary downtime.  

### Considerations

- Data may be up to **12 hours old**, but the **`/pokemon/sync`** endpoint allows forcing an update on demand to keep information current.

---

## 📡 Available Endpoints

The API exposes the following endpoints under the base path `/pokemon`:

| Method | Endpoint | Description | Parameters | Response |
|:--------|:----------|:-------------|:------------|:-----------|
| **GET** | `/highest` | Returns the **N tallest Pokémon**, ordered by height. | `numPokemon` *(int ≥ 1)* | `200 OK` → `PokemonDto` list |
| **GET** | `/heaviest` | Returns the **N heaviest Pokémon**, ordered by weight. | `numPokemon` *(int ≥ 1)* | `200 OK` → `PokemonDto` list |
| **GET** | `/highestExperience` | Returns the **N Pokémon with the highest base experience**. | `numPokemon` *(int ≥ 1)* | `200 OK` → `PokemonDto` list |
| **POST** | `/sync` | Synchronizes the database with the **external PokéAPI**. | — | `204 No Content` |

**Possible error codes:**  
`400 Bad Request` → Invalid parameter (`numPokemon < 1`)  
`500 Internal Server Error` → Internal error or synchronization failure  

**Usage examples:**
```bash
# Get the 10 tallest Pokémon
curl -s "http://localhost:8080/pokemon/highest?numPokemon=10"

# Get the 5 heaviest Pokémon
curl -s "http://localhost:8080/pokemon/heaviest?numPokemon=5"

# Synchronize the database
curl -X POST "http://localhost:8080/pokemon/sync"
```
---

## 🏗️ Architecture and Project Structure

The application follows a typical Spring Boot layered architecture, separating responsibilities to keep the code clean, scalable, and easy to test.

### 🧩 Domain Layer — `Pokemon` Entity

This class represents the **main domain entity**, mapped to the `Pokemons` table in the database.

#### 📘 Description
The `Pokemon` entity stores essential data retrieved from the **PokéAPI**, along with internal system metadata (such as sync date or original JSON).  
It is part of the **Domain layer**, responsible for modeling persistent objects in the application.

#### 🗂️ Model Structure
| Field | Type | Description |
|:-------|:------|:-------------|
| `id` | `String` | Unique identifier generated automatically (UUID). |
| `pokeApiId` | `Integer` | Official Pokémon ID from the PokéAPI. |
| `name` | `String` | Pokémon name. |
| `weight` | `Integer` | Pokémon weight (in hectograms). |
| `height` | `Integer` | Pokémon height (in decimeters). |
| `baseExperience` | `Integer` | Base experience granted when defeating the Pokémon. |
| `rawJson` | `String` *(Lob)* | Full JSON content from the PokéAPI, stored for reference or debugging. |
| `lastSynced` | `Instant` | Timestamp of the last synchronization with the external API. |

#### ⚙️ Key Annotations
- `@Entity` and `@Table(name = "Pokemons")` → Defines the JPA entity and its table.  
- `@Id` → Marks the primary key.  
- `@Lob` → Allows storing the full JSON without size limits.  
- `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` (Lombok) → Auto-generates getters, setters, and constructors.

#### 💡 Notes
- The `id` is generated with `UUID.randomUUID()` when instantiating the object.  
- `rawJson` preserves the original API response.  
- This entity is used by the JPA repository in the **Persistence** layer.

### 🗄️ Persistence Layer — `PokemonRepository`

This interface defines **data access** for the `Pokemon` entity using **Spring Data JPA**.  
It is part of the **Persistence layer**, responsible for interacting with the database efficiently and abstractly.

#### 📘 Description
`PokemonRepository` extends `JpaRepository`, enabling standard CRUD operations on `Pokemon` without manual implementation.  
It also includes custom queries to fetch Pokémon by weight, height, or base experience.

#### 📝 Main Methods

| Method | Description |
|:-------|:------------|
| `Optional<Pokemon> findByPokeApiId(Integer pokeapiId)` | Finds a Pokémon by its official PokéAPI ID. |
| `List<Pokemon> findTopPokemonByWeight(Pageable pageable)` | Returns the heaviest Pokémon, ordered descending by `weight`. |
| `List<Pokemon> findTopPokemonByHeight(Pageable pageable)` | Returns the tallest Pokémon, ordered descending by `height`. |
| `List<Pokemon> findTopPokemonByBaseExperience(Pageable pageable)` | Returns Pokémon with the highest `baseExperience`, ordered descending. |

#### ⚙️ Key Annotations
- `@Repository` → Marks the interface as a Spring component for data persistence.  
- `@Query` → Defines custom JPQL queries to obtain ordered lists.  
- `Pageable` → Allows limiting the number of results returned by each query (pagination).

#### 💡 Notes
- Inherits methods like `save()`, `findAll()`, `delete()`, etc., from `JpaRepository`.  
- Custom queries make it easy to implement `/highest`, `/heaviest`, and `/highestExperience` endpoints efficiently.

### ⚙️ Service Layer — `PokemonService`

This class implements the **business logic** of the application and serves as an intermediary between the controllers (`PokemonController`) and the persistence layer (`PokemonRepository`).  

It is part of the **Service layer**, which encapsulates business rules and transforms entities into DTOs for API responses.

#### 📘 Main Functionalities

| Method | Description |
|:-------|:------------|
| `getHeaviestPokemon(int numPokemon)` | Returns the N heaviest Pokémon, mapped to `PokemonDto`. |
| `getHighestPokemon(int numPokemon)` | Returns the N tallest Pokémon, mapped to `PokemonDto`. |
| `getHighestExperiencesPokemon(int numPokemon)` | Returns the N Pokémon with the highest base experience. |
| `syncDataBase()` | Invokes `PokemonSyncService` to synchronize all Pokémon from the external PokéAPI. |

#### ⚙️ Implementation Details
- **Pagination:** Uses `PageRequest.of(0, numPokemon)` to limit results to N Pokémon.  
- **Transformation:** Converts `Pokemon` entities to `PokemonDto` using `map`.  
- **Error Handling:** Exceptions are captured and thrown as `RuntimeException` for simplicity in controllers.  
- **Dependencies:**  
  - `PokemonRepository` → Data access.  
  - `PokemonSyncService` → External API synchronization.

#### 💡 Notes
- Keeps controllers from directly manipulating the database.  
- Centralizes filtering, sorting, and DTO conversion logic.  
- Can be extended easily for new search criteria or business rules without modifying controllers.

### 🔄 Service Layer — `PokemonSyncService`

This service is responsible for **synchronizing the database** with the full information from the **PokéAPI**. It is part of the **Service layer** and is used internally by `PokemonService`.

#### 📘 Main Functionalities

| Method | Description |
|:-------|:------------|
| `syncAllPokemons()` | Downloads up to 1500 Pokémon from the external API and saves them in the database. Scheduled to run automatically every 12 hours using `@Scheduled`. |
| `fetchAndMapPokemon(PokemonBasic basicInfo)` | Fetches detailed information for each Pokémon and maps it to the `Pokemon` entity. |

#### ⚙️ Implementation Details
- **External API:** Uses `RestClient` to consume `https://pokeapi.co/api/v2/pokemon`.  
- **Pagination:** Limited to 1500 Pokémon with `?limit=1500`.  
- **Mapping:** Each JSON response is converted to `PokemonDetailResponse` using `ObjectMapper` and then to the `Pokemon` entity.  
- **Storage:** All synchronized Pokémon are saved using `pokemonRepository.saveAll(pokemons)`.  
- **Cron Job:** `@Scheduled(cron = "0 0 */12 * * *")` allows automatic database synchronization every 12 hours.  
- **Error Handling:** If a single Pokémon fails, it is filtered out; if the entire list fails, a `RuntimeException` is thrown.

#### 💡 Notes
- Keeps the local database updated with the most recent PokéAPI information.  
- Allows the query services (`PokemonService`) to work with complete and consistent data.

### 🗃️ DTO Layer — Data Transfer Objects

These DTOs are used to **exchange information** between the service layer and controllers, and to map responses from the **PokéAPI** without exposing the `Pokemon` entity directly.

---

#### 1️⃣ `PokemonDto`
Converts a `Pokemon` object to its corresponding DTO and is used in API responses.

| Field | Type | Description |
|:------|:-----|:------------|
| `pokeApiId` | `Integer` | Official Pokémon ID from the PokéAPI. |
| `name` | `String` | Pokémon's name. |
| `weight` | `Integer` | Pokémon's weight (hectograms). |
| `height` | `Integer` | Pokémon's height (decimeters). |
| `baseExperience` | `Integer` | Pokémon's base experience. |

---

#### 2️⃣ `PokemonDetailResponse`
Maps the detailed response from the PokéAPI for a single Pokémon.

| Field | Type | Description |
|:------|:-----|:------------|
| `id` | `Integer` | Pokémon ID in the PokéAPI. |
| `name` | `String` | Pokémon's name. |
| `height` | `Integer` | Pokémon's height. |
| `weight` | `Integer` | Pokémon's weight. |
| `base_experience` | `Integer` | Pokémon's base experience. |

---

#### 3️⃣ `PokemonListResponse`
Maps the Pokémon list response from the PokéAPI.

| Field | Type | Description |
|:------|:-----|:------------|
| `count` | `Integer` | Total number of available Pokémon in the API. |
| `next` | `String` | URL for the next page (pagination). |
| `previous` | `String` | URL for the previous page. |
| `results` | `List<PokemonBasic>` | List of basic Pokémon (name + detail URL). |

**Inner Class `PokemonBasic`:**

| Field | Type | Description |
|:------|:-----|:------------|
| `name` | `String` | Pokémon's name. |
| `url` | `String` | URL to fetch the full details from the PokéAPI. |

---

#### 💡 Notes
- DTOs allow **decoupling** the persistence layer from the REST API.  
- `PokemonDto` is used in the `/highest`, `/heaviest`, and `/highestExperience` endpoints.  
- `PokemonDetailResponse` and `PokemonListResponse` are used internally in `PokemonSyncService` to map PokéAPI information.

### ⚠️ Global Exception Handling — `GlobalExceptionHandler`

The application includes a **global exception handler** to centralize error responses and validate parameters uniformly. It is located in the **web/exceptionHandler** layer.

#### 📘 Main functionalities

| Exception | Handling | HTTP Response | Message |
|:-----------|:------|:---------------|:--------|
| `ConstraintViolationException` | Invalid parameters (e.g., `numPokemon < 1`) | `400 Bad Request` | `{"error": "Invalid parameter"}` |
| `RuntimeException` | General errors | `400 Bad Request` if caused by `IllegalArgumentException` <br> `500 Internal Server Error` for other cases | Exception message or `"Internal server error"` |

#### ⚙️ Implementation details
- `@ControllerAdvice` → Intercepts exceptions from any controller.
- `@ExceptionHandler` → Defines specific methods for different exception types.
- Returns a `ResponseEntity<Map<String,String>>` with a uniform JSON message.
- Improves **API experience** by standardizing errors and HTTP codes.

#### 💡 Notes
- Ensures endpoints do not expose stack traces or internal details to clients.
- Simplifies error handling in controllers by delegating validation and response logic to a single component.

### 🌐 Web / Controller Layer — `PokemonController`

The `PokemonController` exposes the REST API endpoints for querying and synchronizing Pokémon. It belongs to the **Web layer**, responsible for handling HTTP requests, validating parameters, and delegating logic to the corresponding service.

#### 📘 Main Endpoints

| Method | Endpoint | Description | Parameters | Response |
|:--------|:----------|:-------------|:------------|:-----------|
| **GET** | `/highest` | Returns the N tallest Pokémon | `numPokemon` (int ≥ 1) | `200 OK` → List of `PokemonDto` |
| **GET** | `/heaviest` | Returns the N heaviest Pokémon | `numPokemon` (int ≥ 1) | `200 OK` → List of `PokemonDto` |
| **GET** | `/highestExperience` | Returns the N Pokémon with the highest base experience | `numPokemon` (int ≥ 1) | `200 OK` → List of `PokemonDto` |
| **POST** | `/sync` | Synchronizes the database with the PokéAPI | — | `204 No Content` |

#### ⚙️ Implementation Details
- **Parameter validation:** `@Min(1)` ensures that `numPokemon` is ≥ 1.
- **Automatic documentation:** `@Operation`, `@ApiResponses`, and `@Tag` integrate OpenAPI/Swagger to generate API documentation.
- **Service delegation:** Each endpoint calls methods from `PokemonService` to fetch or synchronize data.
- **Response handling:** Uses `ResponseEntity` to control HTTP codes and return consistent JSON.

#### 💡 Notes
- GET endpoints (`/highest`, `/heaviest`, `/highestExperience`) return lists of `PokemonDto`, keeping the persistence layer encapsulated.  
- POST `/sync` updates the database with the latest data from PokéAPI without exposing internal logic.  
- Swagger documentation allows testing endpoints directly via the web interface if `springdoc-openapi` is configured.

## 🧪 Project Tests

The project includes **unit tests** and **integration tests** to ensure the correct functioning of the API and the synchronization with the PokéAPI.

---

### 1️⃣ Integration Tests — `PokemonIntegrationTest`

- **Objective:** Verify that the REST endpoints work correctly with the database.
- **Coverage:**
  - Endpoints `/highest`, `/heaviest`, `/highestExperience`.
  - Endpoint `/sync`.
  - Error handling when invalid parameters are provided.
- **Tools:** `SpringBootTest`, `MockMvc`.
- **Example Verification:**  
  Checks that `/pokemon/highest?numPokemon=3` returns the 3 tallest Pokémon in descending order.

---

### 2️⃣ Unit Tests — `PokemonServiceTest`

- **Objective:** Validate the business logic in `PokemonService`.
- **Coverage:**
  - Methods: `getHeaviestPokemon`, `getHighestPokemon`, `getHighestExperiencesPokemon`, `syncDataBase`.
  - Handling exceptions thrown by the repository or the sync service.
- **Tools:** `Mockito`, `JUnit 5`.
- **Example Verification:**  
  Simulates the repository returning a list of Pokémon and checks that the service correctly transforms them into `PokemonDto`.

---

### 3️⃣ Unit Tests — `PokemonSyncServiceTest`

- **Objective:** Test synchronization with the PokéAPI.
- **Coverage:**
  - Method `syncAllPokemons`.
  - Error handling when fetching the Pokémon list or individual details.
  - Correct saving of Pokémon in the database.
- **Tools:** `Mockito`, `JUnit 5`.
- **Notes:**  
  Simulates PokéAPI responses using `RestClient` and `ObjectMapper`, ensuring the database is updated only with valid Pokémon.

---

### 4️⃣ Unit Tests — `PokemonControllerTest`

- **Objective:** Verify REST controller behavior.
- **Coverage:**
  - Endpoints `/highest`, `/heaviest`, `/highestExperience`, and `/sync`.
  - Correct HTTP responses (`200 OK`, `204 No Content`) and JSON content.
- **Tools:** `Mockito`, `JUnit 5`, `MockMvc`.

---

### 5️⃣ Unit Tests — `GlobalExceptionHandlerTest`

- **Objective:** Validate global exception handling.
- **Coverage:**
  - `RuntimeException` caused by `IllegalArgumentException` → `400 Bad Request`.
  - Generic `RuntimeException` → `500 Internal Server Error`.
- **Notes:** Ensures validation errors and internal failures are handled consistently for API clients.

---

💡 **Summary:**  
The project combines **integration tests** to validate the full API flow with the database and **unit tests** to verify internal service logic and error handling, ensuring high reliability and maintainability.

## 💡 Possible Improvements

- **Custom exceptions:** Create specific error classes to handle different types of failures more clearly and controllably.  
- **More Pokémon attributes:** Include additional information such as types, abilities, sprites, or moves.  
- **Advanced logging:** Implement detailed logging of operations to facilitate debugging and monitoring.  
- **Persistent database:** Replace H2 with a real database (PostgreSQL, MySQL, etc.) for production environments and scalability.

## 📸 Screenshots

1️⃣ Swagger Documentation

<img width="1856" height="1040" alt="image" src="https://github.com/user-attachments/assets/7354377d-2c02-47e3-846b-c3dee91730cb" />

2️⃣ Example GET /highest request

<img width="1132" height="1076" alt="image" src="https://github.com/user-attachments/assets/b7db256c-cebc-410b-80be-25f1f8c4febc" />

3️⃣ Example GET /heaviest request

<img width="1132" height="1067" alt="image" src="https://github.com/user-attachments/assets/d10b0235-45c2-40c3-9c1c-afc9faff12f7" />

4️⃣ Example GET /highestExperience request

<img width="1131" height="1032" alt="image" src="https://github.com/user-attachments/assets/3e7fce47-6a76-41f5-89e8-40acbbe8c957" />

5️⃣ Example POST /sync request

<img width="1100" height="287" alt="image" src="https://github.com/user-attachments/assets/65b50a46-4fe5-4120-9209-32e3a7c29d15" />

6️⃣ Postman Collection File

[Download Postman Collection](postman/PokemonApi.postman_collection.json)

Includes all endpoints ready to test in Postman.

