# CareFlow HMS — Developer Quick Reference Guide

The project is cleanly separated into top-level **`frontend/`** and **`backend/`** directories for clear isolation and simple management.

```
HospitalManagementSystem_Java_React/
├── backend/                              # Java Spring Boot 3 Application
│   ├── src/main/java/com/careflow/hospitalmanagement/
│   │   ├── common/                       # BaseEntity, RolePermissions, Enums
│   │   ├── config/                       # WebMvcConfig, OpenApiConfig
│   │   ├── controller/                   # REST Controllers (/api/v1/*)
│   │   ├── domain/                       # JPA Entities & Domain Models
│   │   ├── dto/                          # Request/Response Data Transfer Objects
│   │   ├── exception/                    # GlobalExceptionHandler & Domain Exceptions
│   │   ├── init/                         # DbInitializer (Auto-Seeding)
│   │   ├── repository/                   # Spring Data JPA Repositories
│   │   ├── security/                     # JwtTokenProvider, Filter, PasswordHasher
│   │   └── service/                      # Clinical & Administrative Business Services
│   ├── src/main/resources/
│   │   ├── static/swagger-ui/            # Custom Swagger UI Themes & Custom Scripts
│   │   ├── templates/emails/             # HTML Notification Email Templates
│   │   ├── application.yml               # H2 (default) & general configuration
│   │   └── application-sqlserver.yml     # SQL Server profile configuration
│   ├── src/test/java/                    # Comprehensive JUnit 5 + Mockito Test Suite
│   ├── pom.xml                           # Maven dependencies & build configuration
│   └── mvnw / mvnw.cmd                   # Cross-platform Maven wrapper
│
└── frontend/                             # React 19 + TypeScript + Vite Application
    ├── src/                              # Feature-sliced frontend modules
    ├── package.json
    └── vite.config.ts
```

---

## 🎨 FRONTEND GUIDE (React 19 + TypeScript + Vite)

### Location
Path: `./frontend`

### 1. Development Server
```bash
cd frontend
npm run dev
```
*Access UI at: `http://localhost:5173`*

### 2. Production Build
```bash
cd frontend
npm run build
```

### 3. Code Formatting & Linting
```bash
cd frontend
npm run format
npm run lint
```

### 4. Run Frontend Tests
```bash
cd frontend
npm test
```

---

## ⚙️ BACKEND GUIDE (Java Spring Boot 3 + Spring Data JPA)

### Location
Path: `./backend`

### 1. Run Spring Boot Server
```bash
# Using Maven Wrapper (runs on port 5000 with H2 database & auto-seeding by default)
cd backend
./mvnw spring-boot:run
# On Windows PowerShell:
.\mvnw.cmd spring-boot:run
```
*Access API & Documentation at:*
- **Developer Swagger Portal**: `http://localhost:5000/swagger`
- **OpenAPI v3 JSON**: `http://localhost:5000/v3/api-docs`
- **H2 Web Console**: `http://localhost:5000/h2-console` (JDBC URL: `jdbc:h2:mem:careflowdb`, user: `sa`)
- **API Base Route**: `http://localhost:5000/api/v1`

### 2. Run with SQL Server Profile (Optional)
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=sqlserver
```

### 3. Run Test Suite (JUnit 5 + Mockito)
```bash
cd backend
./mvnw test
# Windows PowerShell:
.\mvnw.cmd test
```

### 4. Build Production JAR
```bash
cd backend
./mvnw clean package -DskipTests
```
*Outputs runnable JAR in `backend/target/hospital-management-1.0.0.jar`.*
