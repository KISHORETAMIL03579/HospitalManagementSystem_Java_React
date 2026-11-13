package com.careflow.hospitalmanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateServiceTest {

    private EmailTemplateService emailTemplateService;

    @BeforeEach
    void setUp() {
        emailTemplateService = new EmailTemplateService();
    }

    @Test
    @DisplayName("renderStaffInvitation should replace all template placeholders")
    void testRenderStaffInvitation() {
        String result = emailTemplateService.renderStaffInvitation(
                "Dr. John Doe", "johndoe", "Doctor", "http://localhost:5173/activate?token=123", "48 hours");

        assertThat(result).isNotNull();
        assertThat(result).contains("Dr. John Doe");
        assertThat(result).contains("johndoe");
        assertThat(result).contains("Doctor");
        assertThat(result).contains("http://localhost:5173/activate?token=123");
        assertThat(result).doesNotContain("{{FullName}}");
    }

    @Test
    @DisplayName("renderAccountApproved should replace placeholders")
    void testRenderAccountApproved() {
        String result = emailTemplateService.renderAccountApproved("Jane Doe", "Nurse", "http://localhost:5173/login");

        assertThat(result).isNotNull();
        assertThat(result).contains("Jane Doe");
        assertThat(result).contains("Nurse");
        assertThat(result).contains("http://localhost:5173/login");
    }

    @Test
    @DisplayName("renderAccountRejected should handle reason and placeholders")
    void testRenderAccountRejected() {
        String result = emailTemplateService.renderAccountRejected("Applicant X", "Incomplete verification", "http://localhost:5173/contact");

        assertThat(result).isNotNull();
        assertThat(result).contains("Applicant X");
        assertThat(result).contains("Incomplete verification");
        assertThat(result).contains("http://localhost:5173/contact");
    }

    @Test
    @DisplayName("renderPasswordReset should contain reset URL and expiry")
    void testRenderPasswordReset() {
        String result = emailTemplateService.renderPasswordReset("User One", "user@test.com", "http://localhost:5173/reset?token=xyz", 30);

        assertThat(result).isNotNull();
        assertThat(result).contains("User One");
        assertThat(result).contains("user@test.com");
        assertThat(result).contains("http://localhost:5173/reset?token=xyz");
        assertThat(result).contains("30");
    }

    @Test
    @DisplayName("renderAppointmentConfirmation should contain appointment details")
    void testRenderAppointmentConfirmation() {
        String result = emailTemplateService.renderAppointmentConfirmation(
                "Patient Alex", "Dr. Gregory House", "Cardiology", "2026-11-20", "10:30 AM", "Room 402", "http://localhost:5173/portal");

        assertThat(result).isNotNull();
        assertThat(result).contains("Patient Alex");
        assertThat(result).contains("Dr. Gregory House");
        assertThat(result).contains("Cardiology");
        assertThat(result).contains("2026-11-20");
        assertThat(result).contains("10:30 AM");
    }
}
