package com.careflow.hospitalmanagement.domain.entities;

import com.careflow.hospitalmanagement.common.BaseEntity;
import com.careflow.hospitalmanagement.domain.enums.EmailDeliveryStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
public class EmailLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_log_id")
    private Integer emailLogId;

    @Column(name = "staff_registration_request_id")
    private Integer staffRegistrationRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_registration_request_id", insertable = false, updatable = false)
    private StaffRegistrationRequest staffRegistrationRequest;

    @Column(name = "recipient_email", nullable = false, length = 256)
    private String recipientEmail = "";

    @Column(nullable = false, length = 256)
    private String subject = "";

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName = "";

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private EmailDeliveryStatus status = EmailDeliveryStatus.Sent;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(nullable = false)
    private int attempts = 1;

    public Integer getEmailLogId() {
        return emailLogId;
    }

    public void setEmailLogId(Integer emailLogId) {
        this.emailLogId = emailLogId;
    }

    public Integer getStaffRegistrationRequestId() {
        return staffRegistrationRequestId;
    }

    public void setStaffRegistrationRequestId(Integer staffRegistrationRequestId) {
        this.staffRegistrationRequestId = staffRegistrationRequestId;
    }

    public StaffRegistrationRequest getStaffRegistrationRequest() {
        return staffRegistrationRequest;
    }

    public void setStaffRegistrationRequest(StaffRegistrationRequest staffRegistrationRequest) {
        this.staffRegistrationRequest = staffRegistrationRequest;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public EmailDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(EmailDeliveryStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
}
