package com.careflow.hospitalmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI careFlowOpenAPI() {
        final String securitySchemeName = "Bearer";

        return new OpenAPI()
                .info(new Info()
                        .title("CareFlow HMS Developer Portal")
                        .version("v1")
                        .description("""
                            ### 🏥 CareFlow Hospital Management System — Developer Portal
                            **Production-grade REST API for Clinical Operations, Patient Records & Healthcare Workflows**

                            `API Version: v1` &nbsp;|&nbsp; `OpenAPI: 3.0` &nbsp;|&nbsp; `Auth: JWT Bearer` &nbsp;|&nbsp; `Environment: Development / Local`

                            ---

                            ### 📋 Core HMS Modules & Capabilities

                            | Module | Capabilities & Workflows | Canonical Endpoints |
                            | :--- | :--- | :--- |
                            | **🧑‍⚕️ Patients** | Patient Registration, Demographics, Blood Group, Medical Record Tracking | `/api/v1/patients` |
                            | **📅 Appointments** | Scheduling, Availability Slots, Conflict Detection & Status Workflows | `/api/v1/appointments` |
                            | **🩺 Doctors** | Clinical Profiles, Specialties, Weekly Availability & Schedule Exceptions | `/api/v1/doctors` |
                            | **🏢 Departments** | Hospital Organizational Units, Specialty Allocation & Staff Directory | `/api/v1/doctors/departments` |
                            | **🔐 Authentication** | JWT Bearer Tokens, Role Claims, Password Management & Profile Updates | `/api/v1/auth` |
                            | **👥 Admin Users** | Direct Account Management, Role Elevation & Staff Status Activation | `/api/v1/admin/users` |
                            | **📋 Staff Onboarding** | Self-Service Registration, Invitation Validation & Approvals | `/api/v1/staff-registration` |

                            ---

                            ### 🔐 Authentication & Security Guidance
                            - **Quick Staff Login**: Click the green/blue **Authorize** button in the header navigation to authenticate with hospital credentials.
                            - **Supported Roles**: `Admin`, `HospitalManager`, `Doctor`, `Nurse`, `Receptionist`, `Pharmacist`, `LabTechnician`, `Patient`.
                            - **Session Isolation**: JWT tokens are retained strictly in client memory during the session and are cleared automatically upon logout or tab closure.
                            """)
                        .contact(new Contact()
                                .name("CareFlow Engineering Team")
                                .email("support@careflow.local")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token")));
    }
}

