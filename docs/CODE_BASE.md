# Codebase Refactoring Guide

**Date**: 2025-12-02  
**Purpose**: Comprehensive guide to the codebase architecture, data models, and core features for refactoring purposes.  
**Scope**: Functional and architectural aspects (excluding security implementation).

---

## 1. Project Overview

**Poc Wifi Management** is a Spring Boot application designed to manage WiFi access, bandwidth policies, and user quotas. It uses a feature-based package structure, making it modular and easier to refactor or extend.

### 🛠 Technical Stack

- **Java Version**: 21
- **Framework**: Spring Boot 3.5.7
- **Database**: MySQL 8.0
- **ORM**: MyBatis 3.0.5 (XML Mapper approach)
- **Caching**: Redis
- **Messaging**: Zalo ZNS (integration), Email (SMTP)
- **Build Tool**: Maven

---

## 2. Architecture & Folder Structure

The project follows a **Feature-Driven Architecture**. Instead of grouping by layer (controllers, services), it groups by domain feature.

```
src/main/java/com/ads/pocwifimanagement/
├── auth/                   # Authentication specific entities (Role, Group)
├── common/                 # Shared utilities, constants, exceptions
│   ├── models/             # Shared data models (Pagination, BaseAuditEntity)
│   └── utils/              # Helper classes
├── features/               # Core Business Logic
│   ├── bandwidth/          # Bandwidth policies & assignments
│   ├── device/             # Device management & usage tracking
│   ├── portal/             # Captive portal logic & active sessions
│   ├── quota/              # Quota calculations
│   ├── usage/              # Bandwidth usage reporting
│   └── user/               # User management
└── config/                 # Configuration classes (MyBatis, Redis, etc.)
```

### Key Patterns

- **Entity**: POJOs mapping directly to database tables. Most extend `BaseAuditEntity`.
- **Mapper**: MyBatis interfaces for database operations.
- **Service**: Business logic layer.
- **Controller**: REST API endpoints.
- **DTO**: Data Transfer Objects for API requests/responses.
- **Converter**: Helper classes to convert between Entity and DTO.

---

## 3. Core Data Models

### 3.1 User Management (`features/user` & `auth`)

- **User**: The central entity.
    - Fields: `userName`, `email`, `password`, `fullName`, `phone`, `isActive`.
    - Relationships: Has one `UserRole`, belongs to one `UserGroup`.
    - Audit: Extends `BaseAuditEntity` (tracks created/updated by/at).
- **UserGroup** (`auth/entity`):
    - Represents a group of users (e.g., "Students", "Staff").
    - Fields: `name`, `description`.
    - *Note*: Does not support audit fields.
- **UserRole** (`auth/entity`):
    - Represents permissions (e.g., "ADMIN", "USER").

### 3.2 Device Management (`features/device`)

- **Device**:
    - Represents a physical device registered to a user.
    - Fields: `macAddress`, `deviceName`, `deviceType`, `isActive`, `lastSeen`.
    - Relationship: Belongs to a `User`.
- **BandWidthUsage** (Note capitalization):
    - Tracks usage sessions.
    - Fields: `uploadBytes`, `downloadBytes`, `sessionDuration`, `date`.
    - Relationship: Linked to `User` and `Device`.

### 3.3 Bandwidth & Policy (`features/bandwidth`)

- **BandwidthPolicy**:
    - Defines constraints.
    - Fields: `uploadLimit`, `downloadLimit`, `sessionTimeout`, `maxConcurrentConnection`.
- **UserBandwidthPolicy**:
    - Assigns a policy to a specific user.

---

## 4. Module Deep Dive

### 4.1 Portal Module (`features/portal`)

The **Portal** module handles the captive portal logic ("Splash Page").

- **State Management**: Uses an in-memory `ConcurrentHashMap` (`activeUsers`) to track currently connected users.
- **Login Flow**:
    1. Validates credentials against the database.
    2. Generates a specialized **Portal Token** (JWT).
    3. Mocks a MAC address for the session.
    4. Stores session data in memory.
- **Features**:
    - `openInternet(userId)`: Validates user status and grants access (mock logic).
    - `getSession(userId)`: Retrieves current active session details.

### 4.2 Usage & Quota (`features/usage`, `features/quota`)

- **Usage**: Aggregates data from `BandWidthUsage` table to provide reports.
- **Quota**: Likely calculates remaining data based on `BandwidthPolicy` limits vs. actual `BandWidthUsage`.

### 4.3 Infrastructure (`common`)

- **BaseAuditEntity**:
    - Abstract class extended by most entities.
    - Automatic handling of `createdAt`, `updatedAt`, `createdBy`, `updatedBy`.
- **Pagination**:
    - `PageRequest`: Handling page number, size, sorting.
    - `PageResponse`: Wrapper for paginated results.

---

## 5. Configuration & External Integrations

### 5.1 Database (MyBatis)
- **Configuration**: `application.yaml` defines datasource.
- **Mappers**: XML files located in `resources/mybatis/mapper/**/*.xml`.
- **Type Aliases**: Scanned from `com.ads.pocwifimanagement.**.entity`.

### 5.2 Redis
- Used for caching and likely for session management in distributed scenarios (though `PortalService` currently uses an in-memory map, Redis is configured).

### 5.3 Zalo ZNS
- **Purpose**: Sending notifications (likely OTP or alerts) via Zalo.
- **Config**: `zalo.zns` properties in `application.yaml`.

---

## 6. Refactoring Recommendations

If refactoring for a new system, consider:

1.  **Portal State**: The `activeUsers` map in `PortalService` is local to the instance. For a distributed system (multiple server instances), verify if this needs to move to Redis.
2.  **Entity Structure**: `UserGroup` and `UserRole` are in `auth` package while `User` is in `features`. Consider consolidating if the auth module is not being extracted as a separate library.
3.  **Naming Conventions**: `BandWidthUsage` has unconventional capitalization (Capital 'W'). Standardizing to `BandwidthUsage` would be cleaner.
4.  **Hardcoded Mocks**: `PortalService` generates mock MAC addresses. This logic likely needs to be replaced with real hardware integration (Radius/Controller API) in a production environment.

---

**Summary for Refactoring Team**:
This codebase is clean and modular. The primary integration points are the `User` entity (central to everything) and the `PortalService` (central to the WiFi access logic). Focus on replacing the mock hardware logic in `PortalService` with actual controller integrations.
