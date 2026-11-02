# Bus Station Management System (Thema 1)

## Architecture Decisions

### 1. Granular Controller & Service Design

* **Decision:** We chose a **controller-per-entity** and **service-per-entity** structure instead of a single monolithic
  controller.
* **Reason:** This strictly follows the **Single Responsibility Principle (SRP)**. Each controller manages only its own
  HTTP routing, and each service handles only the business logic and validations of its domain entity, keeping files
  modular and maintainable.

### 2. In-Memory Data Storage (`ConcurrentHashMap`)

* **Decision:** We backed our in-memory repositories with `ConcurrentHashMap<ID, T>` rather than a standard `ArrayList`.
* **Reason:**
    * **Thread Safety:** Spring Boot handles requests concurrently across multiple worker threads. `ConcurrentHashMap`
      avoids race conditions and `ConcurrentModificationException` during concurrent reads and writes.
    * **Performance:** Direct ID lookups (`findById`) and deletions run in $\mathcal{O} (1)$ constant time instead
      of $\mathcal{O} (n)$ linear scans.

### 3. Generic In-Memory Repository (`InMemoryRepository<T, ID>`)

* **Decision:** We created a shared generic base class and a `CrudRepository<T, ID>` interface, passing an `idExtractor`
  function (e.g., `BusStation::getId`).
* **Reason:** This eliminates repetitive CRUD boilerplate (**DRY** principle) while allowing services to depend purely
  on interface abstractions (**Dependency Inversion Principle**).

---

## OOP & UML Adjustments

### 1. Hoisting `assignments` to `Staff`

* **Decision:** We moved the `assignments` list up to the abstract `Staff` base class instead of duplicating it in
  `Driver` and `TripManager`.
* **Reason:** Eliminates redundant fields and accessors (**DRY**), while enabling generic staff duty operations without
  downcasting (**Liskov Substitution Principle** and polymorphism).

### 2. Strongly Typed Enums over Magic Strings

* **Decision:** We replaced plain strings with explicit enums (`BusStatus`, `TripStatus`, `DutyRole`).
* **Reason:** Prevents invalid domain states (e.g., typos in trip status) and provides automatic validation at compile
  and HTTP request levels.

### 3. `LocalDateTime` over `String`

* **Decision:** Attributes like `startTime` and `arrivalTime` use `java.time.LocalDateTime`.
* **Reason:** Fulfills the project requirement forbidding strings for temporal data and allows proper date/time
  validation and sorting.

---

## Model Extensions (Requirement 5)

We added new properties to 3 model classes:

1. **`Bus`**: Added `modelName` (`String`) and `manufactureYear` (`int`) for fleet tracking.
2. **`Passenger`**: Added `email` (`String`) for contact and ticketing notifications.
3. **`BusTrip`**: Added `arrivalTime` (`LocalDateTime`) to support trip duration tracking.
   ealth check endpoint: `GET http://localhost:8080/hello` $\rightarrow$ returns `"Die Anwendung funktioniert!"`