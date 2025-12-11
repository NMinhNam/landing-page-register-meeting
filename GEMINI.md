# Project Context: Dang Ky Tham Quan Nhan

## 1. Project Overview
**Name:** `dang_ky_tham_quan_nhan`  
**Type:** Spring Boot Web Application  
**Purpose:** A digital system to manage family visits for military personnel.  
**Primary Actors:**
1.  **Relatives (Public User):** Mobile-first users who scan QR codes to register for visits without login.
2.  **Unit Officers (Admin):** Users who manage approvals, view lists, and generate statistics via a web portal.

## 2. Technical Stack
*   **Language:** Java 25
*   **Framework:** Spring Boot 4.0.0
*   **Build Tool:** Maven
*   **Frontend:** Server-Side Rendering (SSR) with **Thymeleaf** (HTML/CSS/JS).
*   **Database:** MariaDB (JDBC Driver included).
*   **Architecture:** Standard Spring MVC.

## 3. Functional Requirements (Use Cases)

### A. Public Landing Page (Relatives)
*   **Goal:** Simplify the registration process for non-tech-savvy users.
*   **Flow:**
    1.  **Scan QR/Access Link:** Opens the landing page.
    2.  **Introduction:** Shows a 4-step guide (Scan -> Fill Form -> Wait for Approval -> Visit).
    3.  **Registration Form:**
        *   **Soldier Info:** Search/Select Soldier (from `soldier` table) and Unit (from `unit` table).
        *   **Relative Info:** Name, Relationship, Phone, Province.
        *   **Visit Details:** Select Visit Week (1-4).
    4.  **Submission:** Creates a `PENDING` record in `visit_registration`.
    5.  **Confirmation:** Displays a registration code/summary and instructions. No account creation required.

### B. Admin Portal (Unit Officers)
*   **Goal:** Manage visit requests and report statistics.
*   **Features:**
    1.  **Registration List:** Filter by Unit, Week, Province, Status.
    2.  **Approval Workflow:** View details -> Approve/Reject -> Add Note -> System updates status & timestamps.
    3.  **Statistics:** Aggregate data by Province, Week, Unit for reporting.

## 4. Database Schema
Based on `@scritp_db/init.sql`, the system consists of the following key entities:

1.  **`unit`**: Military hierarchy (e.g., Battalion, Company).
    *   `id`, `name`, `parent_id` (Self-referencing FK).
2.  **`soldier`**: Military personnel information.
    *   `id`, `code`, `name`, `unit_id` (FK), `status`.
3.  **`relative`**: Information about the visitor.
    *   `id`, `name`, `phone`, `province`, `id_number`.
4.  **`visit_registration`**: The core transaction record.
    *   `id`, `soldier_id` (FK), `relative_id` (FK), `relationship`, `visit_week` (1-4), `status` (PENDING/APPROVED/REJECTED), `note`, `created_at`, `approved_at`.
5.  **`admin_user`**: Accounts for officers.
    *   `id`, `username`, `password`, `unit_id` (FK), `role` (ADMIN/VIEWER).

## 5. Directory Structure
```text
D:\QuanNhanProject\dang_ky_tham_quan_nhan\
├── docs/                   # Documentation (USE_CASE.md, etc.)
├── scritp_db/              # Database scripts (init.sql)
├── src/
│   ├── main/
│   │   ├── java/com/dang_ky_tham_quan_nhan/  # Spring Boot Application & Logic
│   │   └── resources/
│   │       ├── templates/  # Thymeleaf Views (Landing page, Admin dashboard)
│   │       ├── static/     # CSS, JS, Images
│   │       └── application.properties # Configuration
│   └── test/               # JUnit Tests
└── pom.xml                 # Maven Dependencies
```

## 6. Building and Running
**Prerequisites:** Java 25 SDK, MariaDB Server.

### Configuration
Before running, update `src/main/resources/application.properties` with your MariaDB credentials:
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/your_database_name
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### Commands
*   **Build:** `./mvnw clean install`
*   **Run:** `./mvnw spring-boot:run`

## 7. Development Conventions
*   **UI/UX:** Mobile-first design for public pages. Clear, large fonts.
*   **Security:** Minimal data collection. Secure `admin_user` access.
*   **Code Style:** Standard Java/Spring Boot conventions.