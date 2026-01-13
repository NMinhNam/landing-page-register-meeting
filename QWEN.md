# QWEN.md - Landing Page Register Meeting (Đăng Ký Thăm Quân Nhân)

## Project Overview

**Name:** dang_ky_tham_quan_nhan (Landing Page Register Meeting)  
**Type:** Spring Boot Web Application  
**Purpose:** A digital system to manage family visits for military personnel.  
**Primary Actors:**
1. **Relatives (Public User):** Mobile-first users who scan QR codes to register for visits without login.
2. **Unit Officers (Admin):** Users who manage approvals, view lists, and generate statistics via a web portal.

## Technical Stack

* **Language:** Java 21 (as per pom.xml, though GEMINI.md mentions Java 25)
* **Framework:** Spring Boot 3.4.0
* **Build Tool:** Maven
* **Frontend:** Server-Side Rendering (SSR) with **Thymeleaf** (HTML/CSS/JS)
* **Database:** MariaDB (JDBC Driver included)
* **ORM:** MyBatis 3.0.3
* **Documentation:** OpenAPI/Swagger with springdoc-openapi
* **Architecture:** Feature-driven architecture with Spring MVC

## Project Structure

```
D:\QuanNhanProject\landing-page-register-meeting\
├── docs/                   # Documentation (API_SPEC.md, CODE_BASE.md)
├── postman/                # Postman collection files
├── scritp_db/              # Database scripts (init.sql, mock_data.sql)
├── src/
│   ├── main/
│   │   ├── java/com/dang_ky_tham_quan_nhan/  # Spring Boot Application & Logic
│   │   │   ├── common/     # Shared utilities, config, enums, models
│   │   │   └── features/   # Feature-based modules
│   │   │       ├── auth/   # Authentication functionality
│   │   │       ├── soldier/ # Soldier management
│   │   │       ├── unit/   # Unit hierarchy management
│   │   │       └── visit/  # Visit registration functionality
│   │   └── resources/
│   │       ├── templates/  # Thymeleaf Views (Landing page, Admin dashboard)
│   │       ├── static/     # CSS, JS, Images
│   │       └── application.yaml # Configuration
│   └── test/               # JUnit Tests
├── .mvn/                   # Maven wrapper
├── target/                 # Build output
├── Dockerfile              # Multi-stage Docker build
├── pom.xml                 # Maven Dependencies
├── mvnw, mvnw.cmd          # Maven wrapper scripts
└── acli.exe                # Additional CLI tool
```

## Key Features

### A. Public Landing Page (Relatives)
* **Goal:** Simplify the registration process for non-tech-savvy users.
* **Flow:**
  1. **Scan QR/Access Link:** Opens the landing page.
  2. **Introduction:** Shows a 4-step guide (Scan -> Fill Form -> Wait for Approval -> Visit).
  3. **Registration Form:**
      * **Soldier Info:** Search/Select Soldier (from `soldier` table) and Unit (from `unit` table).
      * **Relative Info:** Name, Relationship, Phone, Province.
      * **Visit Details:** Select Visit Week (1-4).
  4. **Submission:** Creates a `PENDING` record in `visit_registration`.
  5. **Confirmation:** Displays a registration code/summary and instructions. No account creation required.

### B. Admin Portal (Unit Officers)
* **Goal:** Manage visit requests and report statistics.
* **Features:**
  1. **Registration List:** Filter by Unit, Week, Province, Status.
  2. **Approval Workflow:** View details -> Approve/Reject -> Add Note -> System updates status & timestamps.
  3. **Statistics:** Aggregate data by Province, Week, Unit for reporting.

## Database Schema

The system consists of the following key entities:

1. **`unit`**: Military hierarchy (e.g., Battalion, Company).
   * `id`, `name`, `parent_id` (Self-referencing FK).
2. **`soldier`**: Military personnel information.
   * `id`, `code`, `name`, `unit_id` (FK), `status`.
3. **`relative`**: Information about the visitor.
   * `id`, `name`, `phone`, `province`, `id_number`.
4. **`visit_registration`**: The core transaction record.
   * `id`, `soldier_id` (FK), `relative_id` (FK), `relationship`, `visit_week` (1-4), `status` (PENDING/APPROVED/REJECTED), `note`, `created_at`, `approved_at`.
5. **`admin_user`**: Accounts for officers.
   * `id`, `username`, `password`, `unit_id` (FK), `role` (ADMIN/VIEWER).

## API Endpoints

### Public API (No Authentication Required)
* `/public/units` - GET: Retrieve list of military units
* `/public/soldiers` - GET: Search soldiers by name/code and unit
* `/public/registrations` - POST: Submit visit registration
* `/public/registrations/search` - GET: Check registration status by phone or code

### Admin API (Authentication Required)
* `/auth/login` - POST: Authenticate officers
* `/admin/registrations` - GET: List visit registrations with filters
* `/admin/registrations/{id}/status` - PUT: Update registration status (approve/reject)
* `/admin/stats` - GET: Retrieve statistics by province, week, unit

## Building and Running

### Prerequisites
* Java 21 JDK
* Maven 3.6+
* MariaDB Server
* Docker (optional, for containerized deployment)

### Local Development Setup

1. **Database Setup:**
   ```bash
   # Create database and run initialization scripts
   mysql -u root -p < scritp_db/init.sql
   mysql -u root -p < scritp_db/mock_data.sql
   ```

2. **Environment Configuration:**
   Create a `.env` file in the project root or `src/main/resources/`:
   ```
   DB_URL=jdbc:mariadb://localhost:3306/dang_ky_tham_quan_nhan
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   ```

3. **Build and Run:**
   ```bash
   # Using Maven wrapper (recommended)
   ./mvnw clean install
   ./mvnw spring-boot:run
   
   # Or using system Maven
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the Application:**
   * Public landing page: http://localhost:9090/
   * Admin portal: http://localhost:9090/admin (after login)
   * API documentation: http://localhost:9090/swagger-ui.html

### Docker Deployment

```bash
# Build and run with Docker
docker build -t dang_ky_tham_quan_nhan .
docker run -p 9090:9090 -e DB_URL=jdbc:mariadb://your-db-host:3306/dbname -e DB_USERNAME=user -e DB_PASSWORD=password dang_ky_tham_quan_nhan
```

## Development Conventions

* **Code Style:** Standard Java/Spring Boot conventions with feature-based package organization
* **UI/UX:** Mobile-first design for public pages with clear, large fonts
* **Security:** Environment-based configuration for database credentials
* **Testing:** JUnit for unit tests, following Spring Boot testing patterns
* **Documentation:** OpenAPI/Swagger for API documentation, Markdown for project docs

## Configuration Details

The application is configured through `application.yaml` with the following key settings:
* Server runs on port 9090 (configurable via PORT environment variable)
* Database connection via environment variables (DB_URL, DB_USERNAME, DB_PASSWORD)
* HikariCP connection pool with low RAM optimization settings
* Thymeleaf template caching disabled for development
* MyBatis configured with underscore-to-camel-case mapping

## Key Dependencies

* Spring Boot Web Starter
* Spring Boot Thymeleaf Starter
* Spring Boot Validation Starter
* MariaDB JDBC Driver
* MyBatis Spring Boot Starter
* SpringDoc OpenAPI for API documentation