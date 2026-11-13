package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.EmailLog;
import com.careflow.hospitalmanagement.domain.enums.EmailDeliveryStatus;
import com.careflow.hospitalmanagement.repository.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;
    private final EmailLogRepository emailLogRepository;

    @Value("${app.smtp.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("${app.smtp.support-url:http://localhost:5173/contact}")
    private String supportUrl;

    @Value("${app.smtp.sender-email:noreply@careflow.com}")
    private String senderEmail;

    @Value("${app.smtp.sender-name:CareFlow HMS Security}")
    private String senderName;

    @Value("${app.smtp.enable-real-delivery:true}")
    private boolean enableRealDelivery;

    public EmailService(
            JavaMailSender mailSender,
            EmailTemplateService templateService,
            EmailLogRepository emailLogRepository) {
        this.mailSender = mailSender;
        this.templateService = templateService;
        this.emailLogRepository = emailLogRepository;
    }

    public EmailLog sendPasswordResetEmail(String email, String resetToken, String fullName) {
        String resetUrl = frontendBaseUrl + "/reset-password?token=" + resetToken + "&email=" + email;
        String subject = "Reset your CareFlow HMS password";
        String body = templateService.renderPasswordReset(fullName, email, resetUrl, 30);
        return deliverAndLogEmail(email, subject, body, "PasswordResetEmail", null);
    }

    public EmailLog sendPasswordChangedEmail(String email, String fullName) {
        String secureUrl = frontendBaseUrl + "/login";
        String subject = "Your CareFlow HMS password was changed";
        String timestamp = LocalDateTime.now().toString();
        String body = templateService.renderPasswordChanged(fullName, timestamp, secureUrl);
        return deliverAndLogEmail(email, subject, body, "PasswordChangedEmail", null);
    }

    public EmailLog sendStaffInvitationEmail(String email, String fullName, String username, String role, String activationUrl, String expiryTime, Integer requestId) {
        String subject = "You're invited to CareFlow HMS";
        String body = templateService.renderStaffInvitation(fullName, username, role, activationUrl, expiryTime);
        return deliverAndLogEmail(email, subject, body, "StaffInvitationEmail", requestId);
    }

    public EmailLog sendStaffApprovalEmail(String email, String fullName, String roleName, Integer requestId) {
        String loginUrl = frontendBaseUrl + "/login";
        String subject = "Your CareFlow HMS account is approved";
        String body = templateService.renderAccountApproved(fullName, roleName, loginUrl);
        return deliverAndLogEmail(email, subject, body, "AccountApprovedEmail", requestId);
    }

    public EmailLog sendStaffRejectionEmail(String email, String fullName, String reason, Integer requestId) {
        String contact = supportUrl != null && !supportUrl.isBlank() ? supportUrl : frontendBaseUrl + "/contact";
        String subject = "Update on your CareFlow HMS application";
        String body = templateService.renderAccountRejected(fullName, reason, contact);
        return deliverAndLogEmail(email, subject, body, "AccountRejectedEmail", requestId);
    }

    public EmailLog sendRegistrationConfirmationEmail(String email, String fullName, Integer requestId) {
        String contact = supportUrl != null && !supportUrl.isBlank() ? supportUrl : frontendBaseUrl + "/contact";
        String subject = "CareFlow HMS - Staff Registration Request Received";
        String body = templateService.renderStaffInvitation(
                fullName,
                "",
                "Pending Administrative Verification",
                contact,
                "Under review by hospital administration");
        return deliverAndLogEmail(email, subject, body, "RegistrationConfirmation", requestId);
    }

    public EmailLog sendAccountDeactivatedEmail(String email, String fullName, String reason) {
        String contact = supportUrl != null && !supportUrl.isBlank() ? supportUrl : frontendBaseUrl + "/contact";
        String subject = "Important Notice: Your CareFlow HMS account status has changed";
        String body = templateService.renderAccountDeactivated(fullName, reason, contact);
        return deliverAndLogEmail(email, subject, body, "AccountDeactivatedEmail", null);
    }

    private EmailLog deliverAndLogEmail(String recipientEmail, String subject, String body, String templateName, Integer requestId) {
        EmailLog emailLog = new EmailLog();
        emailLog.setStaffRegistrationRequestId(requestId);
        emailLog.setRecipientEmail(recipientEmail);
        emailLog.setSubject(subject);
        emailLog.setTemplateName(templateName);
        emailLog.setSentAt(LocalDateTime.now());
        emailLog.setAttempts(1);

        if (enableRealDelivery) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(senderEmail, senderName);
                helper.setTo(recipientEmail);
                helper.setSubject(subject);
                helper.setText(body, true);

                mailSender.send(message);
                emailLog.setStatus(EmailDeliveryStatus.Sent);
                log.info("Email dispatched successfully to: {}", recipientEmail);
            } catch (Exception e) {
                log.warn("Failed to deliver email to {}: {}. Logging as Failed status.", recipientEmail, e.getMessage());
                emailLog.setStatus(EmailDeliveryStatus.Failed);
                emailLog.setErrorMessage(e.getMessage());
            }
        } else {
            emailLog.setStatus(EmailDeliveryStatus.Sent);
            log.info("Simulated email dispatch (real delivery disabled) to: {}", recipientEmail);
        }

        return emailLogRepository.save(emailLog);
    }
}
