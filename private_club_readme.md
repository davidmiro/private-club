# Closed Club Access System

A backend application built with **Spring Boot**, **PostgreSQL** (running in Docker), and **RESTful APIs** that handles secure, one-time-use QR code access control (using UUIDs) for an exclusive closed club, alongside full CRUD management for members and their access credentials.

## What It Does

1. **One-Time QR Access Validation:**
   * When a guest scans a QR code, the app receives a `UUID` representing the access token.
   * If the UUID exists in the database, the system grants access, returns the member's full name, and automatically rotates the token by generating and saving a *new* UUID (invalidating the old one).
   * If the UUID is not found, access is denied (`401 Unauthorized` or `404 Not Found`).

2. **Member & Credential Management (CRUD):**
   * Exposes REST endpoints to create, read, update, and delete members and manage their QR access credentials.

## Tech Stack & Architecture

* **Language/Framework:** Java 21, Spring Boot (Web, Data JPA)
* **Database:** PostgreSQL 
* **Containerization:** Docker & Docker Compose
* **Identifiers:** `java.util.UUID` for secure, unique one-time QR tokens

## Database Schema Design

The application models a relationship between members and their dynamic access tokens:

* **`members` table:** Stores member details (e.g., `id`, `full_name`, `email`, etc.).
* **`qr_codes` table:** Stores the one-time access tokens mapped to a specific member (e.g., `id`, `member_id`, `token` as `UUID`, `is_active` / expiration tracking).

## API Endpoints Overview

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/access/scan` | Processes incoming QR code UUID, validates it, returns member info, and rotates the token. |
| `GET` | `/api/members` | Retrieves a list of all club members. |
| `POST` | `/api/members` | Adds a new member and issues their initial QR code. |
| `PUT` | `/api/members/{id}` | Updates member information. |
| `DELETE` | `/api/members/{id}` | Removes a member and their associated tokens. |

## How to Run

### Prerequisites

* JDK 21
* Maven
* Docker & Docker Compose

### 1. Start PostgreSQL in Docker

Run the following command in your project root (ensure you have a `docker-compose.yml` set up for PostgreSQL):

```bash
docker-compose up -d
```

### 2. Configure Database Connection

Check your `src/main/resources/application.properties` (or `application.yml`) to ensure connection parameters match your Docker setup:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/club_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
```

### 3. Build and Run the Application

```bash
mvn clean package
java -jar target/closed-club-1.0-SNAPSHOT.jar
```

## Running Tests

```bash
mvn test