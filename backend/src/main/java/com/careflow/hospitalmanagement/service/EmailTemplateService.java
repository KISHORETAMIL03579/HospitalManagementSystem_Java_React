package com.careflow.hospitalmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    public String renderStaffInvitation(String fullName, String username, String role, String activationUrl, String expiryTime) {
        return renderTemplate("StaffInvitationEmail", Map.of(
            "FullName", fullName != null ? fullName : "CareFlow Colleague",
            "Username", username != null ? username : "",
            "Role", role != null ? role : "Staff Member",
            "ActivationUrl", sanitizeUrl(activationUrl),
            "ExpiryTime", expiryTime != null ? expiryTime : "24 hours"
        ));
    }

    public String renderAccountApproved(String fullName, String role, String loginUrl) {
        return renderTemplate("AccountApprovedEmail", Map.of(
            "FullName", fullName != null ? fullName : "CareFlow User",
            "Role", role != null ? role : "Staff Member",
            "LoginUrl", sanitizeUrl(loginUrl)
        ));
    }

    public String renderAccountRejected(String fullName, String reason, String contactUrl) {
        return renderTemplate("AccountRejectedEmail", Map.of(
            "FullName", fullName != null ? fullName : "CareFlow Applicant",
            "Reason", reason != null && !reason.isBlank() ? reason : "Administrative review completed.",
            "ContactUrl", sanitizeUrl(contactUrl)
        ));
    }

    public String renderPasswordReset(String fullName, String email, String resetUrl, int expiryMinutes) {
        return renderTemplate("PasswordResetEmail", Map.of(
            "FullName", fullName != null && !fullName.isBlank() ? fullName : "CareFlow User",
            "Email", email != null ? email : "",
            "ResetUrl", sanitizeUrl(resetUrl),
            "ExpiryMinutes", String.valueOf(expiryMinutes)
        ));
    }

    public String renderPasswordChanged(String fullName, String timestamp, String secureAccountUrl) {
        return renderTemplate("PasswordChangedEmail", Map.of(
            "FullName", fullName != null && !fullName.isBlank() ? fullName : "CareFlow User",
            "Timestamp", timestamp != null ? timestamp : "",
            "SecureAccountUrl", sanitizeUrl(secureAccountUrl)
        ));
    }

    public String renderAppointmentConfirmation(String patientName, String appointmentDate, String doctorName, String department, String location, String referenceNumber, String detailsUrl) {
        return renderTemplate("AppointmentConfirmationEmail", Map.of(
            "PatientName", patientName != null ? patientName : "Patient",
            "AppointmentDate", appointmentDate != null ? appointmentDate : "",
            "DoctorName", doctorName != null ? doctorName : "",
            "Department", department != null ? department : "",
            "Location", location != null ? location : "Main Hospital Campus",
            "ReferenceNumber", referenceNumber != null ? referenceNumber : "",
            "DetailsUrl", sanitizeUrl(detailsUrl)
        ));
    }

    public String renderAppointmentReminder(String patientName, String appointmentDate, String doctorName, String department, String location, String referenceNumber, String detailsUrl) {
        return renderTemplate("AppointmentReminderEmail", Map.of(
            "PatientName", patientName != null ? patientName : "Patient",
            "AppointmentDate", appointmentDate != null ? appointmentDate : "",
            "DoctorName", doctorName != null ? doctorName : "",
            "Department", department != null ? department : "",
            "Location", location != null ? location : "Main Hospital Campus",
            "ReferenceNumber", referenceNumber != null ? referenceNumber : "",
            "DetailsUrl", sanitizeUrl(detailsUrl)
        ));
    }

    public String renderAccountDeactivated(String fullName, String reason, String contactUrl) {
        return renderTemplate("AccountDeactivatedEmail", Map.of(
            "FullName", fullName != null ? fullName : "CareFlow User",
            "Reason", reason != null && !reason.isBlank() ? reason : "Account access has been suspended per administrative policy.",
            "ContactUrl", sanitizeUrl(contactUrl)
        ));
    }

    public String renderTemplate(String templateName, Map<String, String> placeholders) {
        String template = getTemplateContent(templateName);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue() != null ? entry.getValue() : "");
        }
        return template;
    }

    private String getTemplateContent(String templateName) {
        return templateCache.computeIfAbsent(templateName, name -> {
            try {
                ClassPathResource resource = new ClassPathResource("templates/emails/" + name + ".html");
                try (InputStream is = resource.getInputStream()) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                log.warn("Could not load email template from classpath: {}, falling back to default wrapper", name, e);
                return "<html><body><h2>CareFlow Notification</h2><p>{{Reason}}</p><p><a href=\"{{ContactUrl}}\">Contact</a></p></body></html>";
            }
        });
    }

    private static String sanitizeUrl(String url) {
        if (url == null || url.isBlank()) return "#";
        return url.replace("\"", "%22").replace("'", "%27").replace("<", "%3C").replace(">", "%3E");
    }
}
