package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.*;
import com.careflow.hospitalmanagement.domain.enums.EmailDeliveryStatus;
import com.careflow.hospitalmanagement.domain.enums.RegistrationStatus;
import com.careflow.hospitalmanagement.dto.staff.*;
import com.careflow.hospitalmanagement.exception.ResourceNotFoundException;
import com.careflow.hospitalmanagement.repository.*;
import com.careflow.hospitalmanagement.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StaffRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(StaffRegistrationService.class);

    private final StaffRegistrationRequestRepository staffRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final DepartmentRepository deptRepo;
    private final InvitationCodeRepository invitationRepo;
    private final EmailLogRepository emailLogRepo;
    private final PasswordHasher passwordHasher;
    private final EmailService emailService;

    public StaffRegistrationService(
            StaffRegistrationRequestRepository staffRepo,
            UserRepository userRepo,
            RoleRepository roleRepo,
            DepartmentRepository deptRepo,
            InvitationCodeRepository invitationRepo,
            EmailLogRepository emailLogRepo,
            PasswordHasher passwordHasher,
            EmailService emailService) {
        this.staffRepo = staffRepo;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.deptRepo = deptRepo;
        this.invitationRepo = invitationRepo;
        this.emailLogRepo = emailLogRepo;
        this.passwordHasher = passwordHasher;
        this.emailService = emailService;
    }

    public StaffRegistrationRequestDto submitStaffRegistrationRequest(SubmitStaffRegistrationRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim().toLowerCase();

        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered.");
        }
        if (userRepo.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }
        if (staffRepo.existsPendingByEmail(email)) {
            throw new IllegalArgumentException("A registration request for email '" + request.getEmail() + "' is already pending review.");
        }

        Role targetRole = null;
        if (request.getInvitationCode() != null && !request.getInvitationCode().isBlank()) {
            InvitationCode inv = invitationRepo.findByCode(request.getInvitationCode().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or expired invitation code."));
            if (inv.isUsed() || inv.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Invalid or expired invitation code.");
            }
            targetRole = inv.getTargetRole();
        }

        if (targetRole == null && request.getRequestedRoleId() != null) {
            targetRole = roleRepo.findById(request.getRequestedRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid requested role specified."));
        }

        if (targetRole == null) {
            targetRole = roleRepo.findByNameIgnoreCase("Receptionist")
                    .orElseThrow(() -> new IllegalArgumentException("Default role could not be resolved."));
        }

        if ("Admin".equalsIgnoreCase(targetRole.getName()) && (request.getInvitationCode() == null || request.getInvitationCode().isBlank())) {
            throw new IllegalArgumentException("Registration for Administrative role requires a valid invitation code.");
        }

        StaffRegistrationRequest s = new StaffRegistrationRequest();
        s.setFullName(request.getFullName().trim());
        s.setEmail(email);
        s.setUsername(username);
        s.setPasswordHash(passwordHasher.hashPassword(request.getPassword()));
        s.setEmployeeId(request.getEmployeeId() != null && !request.getEmployeeId().isBlank() ? request.getEmployeeId().trim() : null);
        s.setDepartmentId(request.getDepartmentId());
        s.setRequestedRoleId(targetRole.getRoleId());
        s.setInvitationCode(request.getInvitationCode() != null ? request.getInvitationCode().trim() : "");
        s.setStatus(RegistrationStatus.Pending);

        StaffRegistrationRequest created = staffRepo.save(s);
        log.info("STAFF ONBOARDING: Submitted registration request #{} for '{}'", created.getStaffRegistrationRequestId(), created.getEmail());

        emailService.sendRegistrationConfirmationEmail(created.getEmail(), created.getFullName(), created.getStaffRegistrationRequestId());

        return mapToDto(created);
    }

    @Transactional(readOnly = true)
    public StaffRegistrationRequestDto getStaffRegistrationStatus(String email) {
        StaffRegistrationRequest req = staffRepo.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email.trim().toLowerCase())
                .orElse(null);
        return req != null ? mapToDto(req) : null;
    }

    @Transactional(readOnly = true)
    public StaffRegistrationRequestDto getStaffRegistrationRequestById(int id) {
        StaffRegistrationRequest req = staffRepo.findByIdWithDetails(id)
                .orElse(null);
        return req != null ? mapToDto(req) : null;
    }

    @Transactional(readOnly = true)
    public List<StaffRegistrationRequestDto> getStaffRegistrationRequests(RegistrationStatus status) {
        List<StaffRegistrationRequest> list = staffRepo.findAllFiltered(status);
        List<StaffRegistrationRequestDto> dtos = new ArrayList<>();
        for (StaffRegistrationRequest s : list) {
            dtos.add(mapToDto(s));
        }
        return dtos;
    }

    public StaffRegistrationRequestDto approveStaffRegistrationRequest(int requestId, int adminUserId, ApproveStaffRequest request) {
        StaffRegistrationRequest staffReq = staffRepo.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff registration request #" + requestId + " not found."));

        if (staffReq.getStatus() == RegistrationStatus.Approved) {
            throw new IllegalStateException("Staff registration request #" + requestId + " has already been approved.");
        }

        int finalRoleId = (request.getAuthorizedRoleId() != null && request.getAuthorizedRoleId() > 0)
                ? request.getAuthorizedRoleId()
                : staffReq.getRequestedRoleId();

        Role authorizedRole = roleRepo.findById(finalRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Selected authorized role ID " + finalRoleId + " is invalid."));

        // Create or enable user account
        User user = userRepo.findByUsername(staffReq.getUsername())
                .or(() -> userRepo.findByEmailIgnoreCase(staffReq.getEmail()))
                .orElse(null);

        if (user == null) {
            user = new User();
            user.setUsername(staffReq.getUsername());
            user.setEmail(staffReq.getEmail());
            user.setPasswordHash(staffReq.getPasswordHash());
            user.setFullName(staffReq.getFullName());
            user.setEmployeeId(staffReq.getEmployeeId());
            user.setRoleId(authorizedRole.getRoleId());
            user.setRole(authorizedRole);
            user.setActive(true);
            userRepo.save(user);
        } else {
            user.setActive(true);
            user.setRoleId(authorizedRole.getRoleId());
            user.setRole(authorizedRole);
            userRepo.save(user);
        }

        staffReq.setStatus(RegistrationStatus.Approved);
        staffReq.setReviewedByUserId(adminUserId);
        staffReq.setReviewedAt(LocalDateTime.now());
        staffReq.setRejectionReason(null);
        staffReq.setRequestedRoleId(authorizedRole.getRoleId());
        staffRepo.save(staffReq);

        emailService.sendStaffApprovalEmail(staffReq.getEmail(), staffReq.getFullName(), authorizedRole.getName(), staffReq.getStaffRegistrationRequestId());
        log.info("STAFF ONBOARDING: Admin #{} approved registration request #{} for user '{}'",
                adminUserId, requestId, staffReq.getUsername());

        return mapToDto(staffReq);
    }

    public StaffRegistrationRequestDto rejectStaffRegistrationRequest(int requestId, int adminUserId, RejectStaffRequest request) {
        StaffRegistrationRequest staffReq = staffRepo.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff registration request #" + requestId + " not found."));

        staffReq.setStatus(RegistrationStatus.Rejected);
        staffReq.setReviewedByUserId(adminUserId);
        staffReq.setReviewedAt(LocalDateTime.now());
        staffReq.setRejectionReason(request.getReason().trim());
        staffRepo.save(staffReq);

        emailService.sendStaffRejectionEmail(staffReq.getEmail(), staffReq.getFullName(), request.getReason().trim(), staffReq.getStaffRegistrationRequestId());
        log.info("STAFF ONBOARDING: Admin #{} rejected registration request #{} for '{}'. Reason: {}",
                adminUserId, requestId, staffReq.getEmail(), request.getReason());

        return mapToDto(staffReq);
    }

    public StaffRegistrationRequestDto updateStaffRegistrationRequest(int requestId, int adminUserId, UpdateStaffRegistrationRequest request) {
        StaffRegistrationRequest staffReq = staffRepo.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff registration request #" + requestId + " not found."));

        if (request.getFullName() != null && !request.getFullName().isBlank()) staffReq.setFullName(request.getFullName().trim());
        if (request.getEmail() != null && !request.getEmail().isBlank()) staffReq.setEmail(request.getEmail().trim().toLowerCase());
        if (request.getEmployeeId() != null) staffReq.setEmployeeId(request.getEmployeeId().trim());
        if (request.getDepartmentId() != null) staffReq.setDepartmentId(request.getDepartmentId());
        if (request.getRequestedRoleId() != null && request.getRequestedRoleId() > 0) staffReq.setRequestedRoleId(request.getRequestedRoleId());
        if (request.getRejectionReason() != null) staffReq.setRejectionReason(request.getRejectionReason());

        if (request.getStatus() != null) {
            RegistrationStatus newStatus = RegistrationStatus.fromValue(request.getStatus());
            staffReq.setStatus(newStatus);
            if (newStatus == RegistrationStatus.Approved) {
                staffReq.setReviewedAt(LocalDateTime.now());
                staffReq.setReviewedByUserId(adminUserId);
                staffReq.setRejectionReason(null);
            } else if (newStatus == RegistrationStatus.Rejected) {
                staffReq.setReviewedAt(LocalDateTime.now());
                staffReq.setReviewedByUserId(adminUserId);
            }
        }

        staffRepo.save(staffReq);
        log.info("STAFF UPDATE: Admin #{} updated staff request #{}", adminUserId, requestId);

        return mapToDto(staffReq);
    }

    public StaffRegistrationRequestDto retryStaffNotificationEmail(int requestId) {
        StaffRegistrationRequest staffReq = staffRepo.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff registration request #" + requestId + " not found."));

        if (staffReq.getStatus() == RegistrationStatus.Approved) {
            String roleName = staffReq.getRequestedRole() != null ? staffReq.getRequestedRole().getName() : "Staff";
            emailService.sendStaffApprovalEmail(staffReq.getEmail(), staffReq.getFullName(), roleName, requestId);
        } else if (staffReq.getStatus() == RegistrationStatus.Rejected) {
            emailService.sendStaffRejectionEmail(staffReq.getEmail(), staffReq.getFullName(),
                    staffReq.getRejectionReason() != null ? staffReq.getRejectionReason() : "Administrative review completed.", requestId);
        } else {
            emailService.sendRegistrationConfirmationEmail(staffReq.getEmail(), staffReq.getFullName(), requestId);
        }

        log.info("RETRY EMAIL: Dispatched notification retry for request #{} ({})", requestId, staffReq.getEmail());
        return mapToDto(staffReq);
    }

    private StaffRegistrationRequestDto mapToDto(StaffRegistrationRequest s) {
        StaffRegistrationRequestDto dto = new StaffRegistrationRequestDto();
        dto.setId(s.getStaffRegistrationRequestId());
        dto.setFullName(s.getFullName());
        dto.setEmail(s.getEmail());
        dto.setUsername(s.getUsername());
        dto.setEmployeeId(s.getEmployeeId());
        dto.setDepartmentId(s.getDepartmentId());
        dto.setDepartmentName(s.getDepartment() != null ? s.getDepartment().getName() : null);
        dto.setRequestedRoleId(s.getRequestedRoleId());
        dto.setRequestedRoleName(s.getRequestedRole() != null ? s.getRequestedRole().getName() : "Staff");
        dto.setInvitationCode(s.getInvitationCode() != null ? s.getInvitationCode() : "");
        dto.setStatus(s.getStatus());
        dto.setSubmittedAt(s.getCreatedAt());
        dto.setReviewedAt(s.getReviewedAt());
        dto.setReviewedByName(s.getReviewedByUser() != null ? s.getReviewedByUser().getFullName() : null);
        dto.setRejectionReason(s.getRejectionReason());
        dto.setEmailStatus(EmailDeliveryStatus.Sent);

        Optional<EmailLog> logOpt = emailLogRepo.findTopByStaffRegistrationRequestIdOrderBySentAtDesc(s.getStaffRegistrationRequestId());
        if (logOpt.isPresent()) {
            EmailLog logEntry = logOpt.get();
            dto.setEmailStatus(logEntry.getStatus());
            dto.setLastEmailAttempt(logEntry.getSentAt());
            dto.setEmailErrorMessage(logEntry.getErrorMessage());
            dto.setEmailLogId(logEntry.getEmailLogId());
        }

        return dto;
    }
}
