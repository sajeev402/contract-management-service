# 📄 Contract Management Service

A comprehensive backend service for managing the lifecycle of business contracts and their associated line items. This system allows for the creation, approval, renewal, and management of contracts according to strict business rules.

---

# 🚀 Features

- **Contract Management** – Create, read, update, and delete contracts.
- **Contract Line Items** – Manage line items associated with a contract with automatic total value calculation (Gross & Net).
- **Lifecycle Management** – Transition contracts through defined states (`DRAFT` → `PENDING_APPROVAL` → `ACTIVE` → `EXPIRED` / `TERMINATED`).
- **Approval Workflow** – Specialized endpoint for approving contracts and transitioning them from `PENDING_APPROVAL` to `ACTIVE`.
- **Contract Renewal** – Renew existing contracts to generate a new active contract while automatically referencing the parent contract.
- **Expiring Contracts Management** – Retrieve a list of contracts approaching their expiration date.
- **Search, Filtering & Pagination** – Search and paginate through existing contracts.

---

# 🛠 Tech Stack

- **Java 21** – Modern Java features and performance.
- **Spring Boot 3.x** – Robust and rapid application development.
- **Spring Data JPA** – Seamless database interactions.
- **PostgreSQL** – Reliable relational database system.
- **Flyway** – Database migration and versioning.
- **H2 Database** – In-memory database used for integration testing.
- **JUnit 5 & Mockito** – Comprehensive unit and integration testing.
- **Lombok** – Boilerplate reduction.
- **OpenAPI / Swagger UI** – API documentation and exploration.

---

# 🏗 Architecture

This service is built using a standard **Layered Architecture** (**Controller → Service → Repository**) commonly used in enterprise Spring Boot applications.

### Design Decisions

- **Separation of Concerns** – Business logic (Service) is completely decoupled from HTTP transport details (Controller) and database operations (Repository), improving maintainability and testability.
- **DTO Pattern** – Data Transfer Objects (DTOs) with manual mappers decouple the external API contract from internal JPA entities, preventing accidental exposure of database fields.
- **Centralized Error Handling** – A `@RestControllerAdvice` global exception handler catches domain-specific exceptions (such as `ResourceNotFoundException` and `IllegalStateException`) and translates them into standardized HTTP error responses.

---

# ⚙ Setup and Run

## Prerequisites

- Java 21 JDK
- Maven (or use the provided Maven Wrapper `./mvnw`)
- PostgreSQL running locally on port **5432**
- Database named **contractdb**

---

## 1. Configure Database

Update your PostgreSQL credentials inside:

```
src/main/resources/application.yml
```

Flyway will automatically execute SQL migrations and provision the database schema during application startup.

---

## 2. Build and Test

```bash
./mvnw clean test
```

---

## 3. Run the Application

```bash
./mvnw spring-boot:run
```

---

## Swagger UI

After the application starts successfully, access Swagger documentation at:

```
http://localhost:8080/swagger-ui/index.html
```

---

# 📌 API Overview

## Contracts

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/contracts` | Create a new contract |
| GET | `/api/contracts/{id}` | Retrieve contract by ID |
| GET | `/api/contracts` | Get paginated contracts with optional filtering |
| PUT | `/api/contracts/{id}` | Update contract |
| DELETE | `/api/contracts/{id}` | Soft delete contract |

---

## Contract Lifecycle

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/contracts/{id}/submit` | Submit draft contract for approval |
| POST | `/api/contracts/{id}/approve` | Approve contract |
| POST | `/api/contracts/{id}/reject` | Reject contract |
| POST | `/api/contracts/{id}/terminate` | Terminate active contract |
| POST | `/api/contracts/{id}/renew` | Renew contract |
| GET | `/api/contracts/expiring` | Find expiring contracts |

---

## Line Items

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/contracts/{contractId}/items` | Add line item |
| GET | `/api/contracts/{contractId}/items` | Retrieve line items |
| DELETE | `/api/contracts/{contractId}/items/{itemId}` | Remove line item |

---

# 📋 Assumptions

Where the specification was silent or ambiguous, the following assumptions were made:

### Financial Calculations

- All line items within a single contract share the same currency.
- Line item values are calculated as:

```
(Quantity × Unit Price) − Discount
```

### State Constraints

- Line items can only be added, updated, or removed while the contract is in **DRAFT** status.
- Only **DRAFT** contracts can be submitted for approval.
- Only **PENDING_APPROVAL** contracts can be approved or rejected.
- Only **ACTIVE** contracts can be renewed or terminated.

### Renewals

Renewing an **ACTIVE** contract creates a brand-new contract in the **DRAFT** state while maintaining a reference to the original contract using `parent_contract_id`.

---

# 🏛 Design Decisions

| Component | Selected Approach | Rejected Alternative | Reasoning |
|------------|------------------|----------------------|-----------|
| Value Storage | Dynamic calculation persisted directly to the Contract entity | On-the-fly calculation during GET requests | Guarantees fast read performance and enables database-level sorting/filtering by total value |
| Deletion Semantics | Soft deletion using a `deleted` flag | Hard deletion | Preserves audit history and prevents orphaned historical data |
| Approval Modelling | Finite State Machine using dedicated endpoints (`/submit`, `/approve`) | Generic PUT status updates | Enforces business rules and prevents invalid state transitions |
| Expiry Mechanism | On-demand REST endpoint (`/expiring`) | Scheduled cron job | Simplifies deployment without background threads |

---

# ⚖ Trade-offs

Given the assignment scope, the following simplifications were made:

- **Security Omitted** – Authentication and Authorization are intentionally excluded.
- **Manual Mapping** – Manual DTO-to-Entity mapping is used instead of MapStruct.
- **Basic Search** – Search and pagination use Spring Data JPA with `Pageable` rather than a dynamic Criteria API.

---

# 🚀 Future Improvements

If additional development time were available, the following enhancements would be prioritized:

- Integrate Spring Security with OAuth2/JWT for Role-Based Access Control.
- Introduce Spring Application Events or Apache Kafka for asynchronous lifecycle processing.
- Implement scheduled background jobs to automatically expire contracts.
- Add Docker and Docker Compose support for simplified deployment.
  
---

# 👨‍💻 Author

**Sajeevarao Thota**

GitHub: https://github.com/sajeev402
