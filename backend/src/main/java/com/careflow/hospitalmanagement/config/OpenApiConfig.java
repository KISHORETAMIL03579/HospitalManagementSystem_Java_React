package com.careflow.hospitalmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.tags.Tag;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI careFlowOpenAPI() {
        final String securitySchemeName = "Bearer";

        return new OpenAPI()
                .info(new Info()
                        .title("CareFlow Hospital Management API")
                        .version("v1")
                        .description("CareFlow Hospital Management System — Production REST API for clinical operations, patient records, and healthcare workflows.")
                        .contact(new Contact()
                                .name("CareFlow Engineering Team")
                                .email("support@careflow.com")))
                .tags(List.of(
                        new Tag().name("Authentication").description("Authentication and Account Management Endpoints"),
                        new Tag().name("Patients").description("Patient Directory and Registration Endpoints"),
                        new Tag().name("Doctors").description("Doctor Management, Scheduling, Availability & Exception Endpoints"),
                        new Tag().name("Appointments").description("Appointment Scheduling, Conflict Checking and Status Management Endpoints"),
                        new Tag().name("Staff Registration").description("Staff Self-Service Onboarding and Administrative Review Endpoints"),
                        new Tag().name("Admin - Users").description("Direct User Administration Endpoints")
                ))
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

