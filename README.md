# CareFlow Hospital Management System (HMS)

> A modern, enterprise-grade **Hospital Management System** built with **Java Spring Boot 3**, **Spring Security**, **Spring Data JPA**, **H2 / SQL Server**, and **React 19 + TypeScript (Vite)** adhering to **Clean Architecture** and **Feature-Sliced Frontend Design**.

[![Java Spring Boot 3 Build & Test](https://img.shields.io/badge/Java-21%20%7C%20Spring%20Boot%203.3-green?logo=springboot)](https://github.com/KISHORETAMIL03579/HospitalManagementSystem_Java_React)
![React 19](https://img.shields.io/badge/React-19.0-blue?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8-blue?logo=typescript)
![Database](https://img.shields.io/badge/Database-H2%20%7C%20SQL%20Server-red)

---

## 🏛️ System Architecture

```
                       ┌───────────────────────────────────────┐
                       │          React + TS (Vite)            │
                       │ TanStack Query │ React Hook Form │ Zod│
                       └───────────────────┬───────────────────┘
                                           │ HTTP / JSON (/api/v1)
                                           ▼
                       ┌───────────────────────────────────────┐
                       │        Spring Boot REST Controllers   │
                       │   Spring Security JWT / CORS / Actuator│
                       └───────────────────┬───────────────────┘
                                           │
                                           ▼
                       ┌───────────────────────────────────────┐
                       │             Service Layer             │
                       │  Business Logic / DTO Mappings / Auth │
                       └───────────────────┬───────────────────┘
                                           │
                                           ▼
                       ┌───────────────────────────────────────┐
                       │             Domain Model              │
                       │ Entities / Business Rules / Enums     │
                       └───────────────────┬───────────────────┘
                                           ▲
                                           │
                       ┌───────────────────┴───────────────────┐
                       │     Spring Data JPA Repositories      │
                       │  Hibernate ORM / JPQL / Specifications│
                       └───────────────────┬───────────────────┘
                                           │
                                           ▼
                               ┌───────────────────────┐
                               │  H2 (Dev) / SQL Server│
                               └───────────────────────┘
```

---

## 🚀 Key Features

- **Dynamic RBAC & Role Hierarchy**: 8 dynamic roles (`Admin`, `HospitalManager`, `Doctor`, `Nurse`, `Receptionist`, `Pharmacist`, `LabTechnician`, `Patient`) with granular permissions and numeric hierarchy levels.
- **Enterprise Security**: PBKDF2 HMAC-SHA256 password hashing (100,000 iterations), stateless JWT Bearer token authentication, refresh token rotation, and Spring Security method authorization (`@PreAuthorize`).
- **Staff Onboarding & Invitation Workflows**: Secure invitation codes with role/email bindings, self-service applicant registration requests, and administrative approval workflows with automated HTML email notifications.
- **Patient Management & Automated MRN Generation**: Formatted unique Medical Record Numbers (e.g. `MRN-2026-00001`), demographics, medical history, emergency contacts, and paginated search.
- **Doctor Directory, Schedules & Exceptions**: Clinical availability engine evaluating recurring weekly hours, leave exceptions, slot bookings, and real-time conflict checking.
- **Appointment Scheduling**: Multi-state lifecycle workflow (`Pending`, `Confirmed`, `CheckedIn`, `InConsultation`, `Completed`, `Cancelled`, `NoShow`) with conflict prevention.
- **Interactive OpenAPI / Swagger UI**: Built-in developer portal with custom CareFlow branding and header JWT authentication at `http://localhost:5000/swagger`.
- **Zero-Config Developer Experience**: Embedded H2 database with automatic schema generation and seed data (`DbInitializer`) ready on first launch; seamless optional switch to Microsoft SQL Server via Spring profiles.

---

## 📋 Default Credentials (Auto-Seeded)

| Role | Username / Email | Password | Access Scope |
|------|-----------------|----------|--------------|
| **Admin** | `admin` / `admin@careflow.com` | `Admin123!` | System-wide administrative controls |
| **Hospital Manager** | `manager` / `manager@careflow.com` | `Manager123!` | Operations, staff requests, schedules |
| **Doctor** | `doctor` / `doctor@careflow.com` | `Doctor123!` | Clinical records, appointments, schedules |
| **Nurse** | `nurse` / `nurse@careflow.com` | `Nurse123!` | Patient care, appointments |
| **Receptionist** | `receptionist` / `receptionist@careflow.com` | `Reception123!` | Patient intake, scheduling, check-in |
| **Pharmacist** | `pharmacist` / `pharmacist@careflow.com` | `Pharmacy123!` | Pharmacy operations |
| **Lab Technician** | `labtech` / `labtech@careflow.com` | `Lab123!` | Laboratory diagnostics |
| **Patient** | `patient` / `patient@careflow.com` | `Patient123!` | Patient portal, personal bookings |

---

## 🛠️ Quick Start

### Prerequisites
- **Java**: JDK 21+ (Compatible with Oracle JDK / OpenJDK 21, 25, 27)
- **Node.js**: v20+ or v22+
- **npm**: v10+

### 1. Clone & Set Up
```bash
git clone https://github.com/KISHORETAMIL03579/HospitalManagementSystem_Java_React.git
cd HospitalManagementSystem_Java_React
```

### 2. Run the Spring Boot Backend
```bash
# Using the Maven wrapper (starts on http://localhost:5000)
cd backend
./mvnw spring-boot:run
# On Windows PowerShell:
.\mvnw.cmd spring-boot:run
```
- **API Base**: `http://localhost:5000/api/v1`
- **Swagger UI**: `http://localhost:5000/swagger`
- **H2 Console**: `http://localhost:5000/h2-console` (JDBC URL: `jdbc:h2:mem:careflowdb`, user: `sa`, password: blank)

### 3. Run the React Frontend
```bash
# In a separate terminal
cd frontend
npm install
npm run dev
```
- **Frontend App**: `http://localhost:5173`

---

## 🧪 Testing

### Backend (JUnit 5 + Mockito)
```bash
cd backend
./mvnw test
# Windows PowerShell:
.\mvnw.cmd test
```

### Frontend (Vitest)
```bash
cd frontend
npm test
```

---

## 📄 License
This project is licensed under the MIT License.
