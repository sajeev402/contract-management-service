# Contract Management Service

A comprehensive backend service for managing the lifecycle of business contracts and their associated line items. This system allows for the creation, approval, renewal, and management of contracts according to strict business rules.

## Features

*   **Contract Management**: Create, read, update, and delete contracts.
*   **Contract Line Items**: Manage line items associated with a contract with automatic total value calculation (Gross & Net).
*   **Lifecycle Management**: Transition contracts through defined states (`DRAFT` -> `PENDING_APPROVAL` -> `ACTIVE` -> `EXPIRED`/`TERMINATED`).
*   **Approval Workflow**: Specialized endpoint for approving contracts and transitioning them from `PENDING_APPROVAL` to `ACTIVE`.
*   **Contract Renewal**: Renew existing contracts to generate a new active contract, automatically referencing the parent contract.
*   **Expiring Contracts Management**: Retrieve a list of contracts approaching their expiration date.
*   **Search, Filtering & Pagination**: Easily search and paginate through existing contracts.

## Tech Stack

*   **Java 21**: Modern Java features and performance.
*   **Spring Boot 3.x**: Robust and rapid application development.
*   **Spring Data JPA**: Seamless database interactions.
*   **PostgreSQL**: Reliable relational database system.
*   **Flyway**: Database migration and versioning.
*   **H2 Database**: In-memory database used for integration testing.
*   **JUnit 5 & Mockito**: Comprehensive unit and integration testing.
*   **Lombok**: Boilerplate reduction.
*   **OpenAPI / Swagger UI**: API documentation and exploration.

## Project Structure

*   `controller/` - REST endpoints and API definitions.
*   `service/` - Core business logic and rules implementation.
*   `repository/` - Data access interfaces via Spring Data JPA.
*   `model/` - JPA Entity definitions and database mapping.
*   `dto/` - Data Transfer Objects for API requests and responses.
*   `mapper/` - Manual mappers for Entity-DTO conversion.
*   `exception/` - Custom exceptions and Global Exception Handler.
*   `enums/` - Application constants (Status, Type).

## Setup & Execution

### Prerequisites

*   Java 21 JDK
*   Maven (or use the provided `mvnw` wrapper)
*   PostgreSQL running on `localhost:5432`

### Database Configuration

Ensure PostgreSQL is running and update `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/contractdb
    username: your_username
    password: your_password
```

Note: Flyway will automatically apply migrations upon application startup to structure the database schema.

### Running the Application

Using Maven wrapper:
```bash
./mvnw spring-boot:run
```

## API Overview

Once the application is running (default port: `8080`), you can access the Swagger UI for full API documentation:
**Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

### Key Endpoints

*   **Contracts:**
    *   `POST /api/contracts` - Create a new contract.
    *   `GET /api/contracts/{id}` - Retrieve a contract by ID.
    *   `GET /api/contracts` - Get a paginated list of contracts (with optional filtering).
    *   `PUT /api/contracts/{id}` - Update a contract.
    *   `DELETE /api/contracts/{id}` - Soft-delete a contract.
*   **Contract Lifecycle:**
    *   `POST /api/contracts/{id}/submit` - Submit a draft contract for approval.
    *   `POST /api/contracts/{id}/approve` - Approve a contract (transition to ACTIVE).
    *   `POST /api/contracts/{id}/reject` - Reject a contract.
    *   `POST /api/contracts/{id}/terminate` - Terminate an active contract.
    *   `POST /api/contracts/{id}/renew` - Renew a contract.
    *   `GET /api/contracts/expiring` - Find expiring contracts.
*   **Line Items:**
    *   `POST /api/contracts/{contractId}/items` - Add a line item.
    *   `GET /api/contracts/{contractId}/items` - Get all items for a contract.
    *   `DELETE /api/contracts/{contractId}/items/{itemId}` - Remove a line item.

## Business Rules & Assumptions

*   **Financial Calculations**: Contract Total Value is calculated as the sum of all associated line items. Line Item values are calculated as `(Quantity * Unit Price) - Discount`.
*   **State Constraints**: 
    *   Line items cannot be added or removed unless the contract is in `DRAFT` status.
    *   Only `DRAFT` contracts can be submitted for approval.
    *   Only `PENDING_APPROVAL` contracts can be approved or rejected.
    *   Only `ACTIVE` contracts can be renewed or terminated.
*   **Soft Deletion**: Contracts are not hard-deleted from the database. A `deleted` flag is updated instead to preserve audit history.
*   **Renewals**: Renewing an `ACTIVE` contract creates a copy with a new `DRAFT` status while setting the `parent_contract_id` to maintain history.
