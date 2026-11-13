package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.dto.auth.*;
import com.careflow.hospitalmanagement.domain.entities.*;
import com.careflow.hospitalmanagement.domain.enums.RegistrationStatus;
import com.careflow.hospitalmanagement.domain.enums.UserRole;
import com.careflow.hospitalmanagement.exception.AccountInactiveException;
import com.careflow.hospitalmanagement.exception.AccountPendingApprovalException;
import com.careflow.hospitalmanagement.exception.AccountRejectedException;
import com.careflow.hospitalmanagement.exception.ResourceNotFoundException;
import com.careflow.hospitalmanagement.repository.*;
import com.careflow.hospitalmanagement.security.JwtTokenProvider;
import com.careflow.hospitalmanagement.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InvitationCodeRepository invitationCodeRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final StaffRegistrationRequestRepository staffRegistrationRequestRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            InvitationCodeRepository invitationCodeRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            StaffRegistrationRequestRepository staffRegistrationRequestRepository,
            PasswordHasher passwordHasher,
            JwtTokenProvider jwtTokenProvider,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.invitationCodeRepository = invitationCodeRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.staffRegistrationRequestRepository = staffRegistrationRequestRepository;
        this.passwordHasher = passwordHasher;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsernameOrEmail().trim();
        Optional<User> userOpt = userRepository.findByEmailOrUsernameWithRole(identifier);

        if (userOpt.isEmpty()) {
            Optional<StaffRegistrationRequest> staffOpt = staffRegistrationRequestRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(identifier);
            if (staffOpt.isPresent()) {
                StaffRegistrationRequest staff = staffOpt.get();
                if (staff.getStatus() == RegistrationStatus.Pending) {
                    throw new AccountPendingApprovalException(
                        "Your staff onboarding request is currently awaiting administrator review. You will receive an email notification once your request has been reviewed.");
                }
                if (staff.getStatus() == RegistrationStatus.Rejected) {
                    throw new AccountRejectedException(
                        "Your staff registration request was not approved. Please contact the hospital administration office for further details.");
                }
            }
            throw new NoSuchElementException("Account '" + request.getUsernameOrEmail() + "' is not registered in CareFlow HMS.");
        }

        User user = userOpt.get();

        if (!passwordHasher.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect password provided. Please verify your password.");
        }

        if (!user.isActive()) {
            throw new AccountInactiveException(
                "Your account has been created but is currently inactive. You cannot sign in until your account is activated by an authorized hospital administrator. Please contact your hospital administrator or the IT support team to request activation.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setRefreshToken(jwtTokenProvider.generateRefreshToken());
        user.setRefreshTokenExpiryTime(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);
        return new AuthResponse(token, user.getRefreshToken(), LocalDateTime.now().plusMinutes(120), mapToUserDto(user));
    }

    public AuthResponse register(RegisterUserRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered.");
        }

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }

        if (request.getInvitationCode() == null || request.getInvitationCode().isBlank()) {
            throw new IllegalArgumentException("Invitation code is required for staff registration.");
        }

        InvitationCode invitation = invitationCodeRepository.findByCode(request.getInvitationCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or unrecognized invitation code."));

        if (invitation.isUsed()) {
            throw new IllegalArgumentException("This invitation code has already been used.");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This invitation code has expired.");
        }

        if (invitation.getBoundEmail() != null && !invitation.getBoundEmail().isBlank() &&
                !invitation.getBoundEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Invitation code is restricted to a specific email address.");
        }

        if (invitation.getBoundEmployeeId() != null && !invitation.getBoundEmployeeId().isBlank() &&
                (request.getEmployeeId() == null || !invitation.getBoundEmployeeId().equalsIgnoreCase(request.getEmployeeId().trim()))) {
            throw new IllegalArgumentException("Invitation code is restricted to a specific employee ID.");
        }

        Role targetRole = invitation.getTargetRole();
        if (targetRole == null && invitation.getTargetRoleId() != null) {
            targetRole = roleRepository.findById(invitation.getTargetRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Authorized role associated with invitation code could not be resolved."));
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHasher.hashPassword(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setEmployeeId(request.getEmployeeId() != null && !request.getEmployeeId().isBlank() ? request.getEmployeeId().trim() : null);
        user.setRoleId(targetRole.getRoleId());
        user.setRole(targetRole);
        user.setActive(true);
        user.setRefreshToken(jwtTokenProvider.generateRefreshToken());
        user.setRefreshTokenExpiryTime(LocalDateTime.now().plusDays(7));

        User savedUser = userRepository.save(user);

        invitation.setUsed(true);
        invitation.setUsedAt(LocalDateTime.now());
        invitation.setUsedByUserId(savedUser.getUserId());
        invitationCodeRepository.save(invitation);

        log.info("SECURITY AUDIT: User '{}' registered with role '{}' using invitation code '{}'",
                user.getUsername(), targetRole.getName(), request.getInvitationCode());

        String token = jwtTokenProvider.generateToken(savedUser);
        return new AuthResponse(token, savedUser.getRefreshToken(), LocalDateTime.now().plusMinutes(120), mapToUserDto(savedUser));
    }

    public AuthResponse refreshToken(String refreshToken) {
        String cleanToken = refreshToken != null ? refreshToken.replace("\"", "").trim() : "";
        User user = userRepository.findAll().stream()
                .filter(u -> cleanToken.equals(u.getRefreshToken()) && u.getRefreshTokenExpiryTime() != null && u.getRefreshTokenExpiryTime().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired refresh token."));

        user.setRefreshToken(jwtTokenProvider.generateRefreshToken());
        user.setRefreshTokenExpiryTime(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);
        return new AuthResponse(token, user.getRefreshToken(), LocalDateTime.now().plusMinutes(120), mapToUserDto(user));
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(int userId) {
        User user = userRepository.findByIdWithRole(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToUserDto(user);
    }

    public UserDto updateProfile(int userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmailIgnoreCase(newEmail)) {
                    throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already in use.");
                }
                user.setEmail(newEmail);
            }
        }

        user.setPhone(request.getPhone());
        user.setTimeFormat(request.getTimeFormat());
        if (request.getTimeZone() != null) user.setTimeZone(request.getTimeZone());
        if (request.getLanguage() != null) user.setLanguage(request.getLanguage());

        userRepository.save(user);
        return mapToUserDto(user);
    }

    public void changePassword(int userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!passwordHasher.verifyPassword(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect current password.");
        }

        user.setPasswordHash(passwordHasher.hashPassword(request.getNewPassword()));
        userRepository.save(user);

        emailService.sendPasswordChangedEmail(user.getEmail(), user.getFullName());
    }

    @Transactional(readOnly = true)
    public List<RoleDto> getRoles(boolean isAdmin) {
        List<Role> roles = roleRepository.findAllActiveWithPermissions();
        List<RoleDto> result = new ArrayList<>();
        for (Role r : roles) {
            if (!isAdmin && "Admin".equalsIgnoreCase(r.getName())) {
                continue;
            }
            RoleDto dto = new RoleDto();
            dto.setRoleId(r.getRoleId());
            dto.setName(r.getName());
            dto.setDescription(r.getDescription());
            dto.setLevel(r.getLevel());
            dto.setParentRoleId(r.getParentRoleId());
            if (r.getRolePermissions() != null) {
                List<String> perms = new ArrayList<>();
                r.getRolePermissions().forEach(rp -> {
                    if (rp.getPermission() != null && rp.getPermission().getName() != null) {
                        perms.add(rp.getPermission().getName());
                    }
                });
                dto.setPermissions(perms);
            }
            result.add(dto);
        }
        return result;
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString().replace("-", "");
            PasswordResetToken prt = new PasswordResetToken();
            prt.setEmail(email);
            prt.setTokenHash(passwordHasher.hashPassword(token));
            prt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            passwordResetTokenRepository.save(prt);

            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getFullName());
        });
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        String email = request.getEmail().trim().toLowerCase();
        PasswordResetToken prt = passwordResetTokenRepository.findTopByEmailIgnoreCaseAndIsUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token."));

        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token has expired.");
        }

        if (!passwordHasher.verifyPassword(request.getToken(), prt.getTokenHash())) {
            throw new IllegalArgumentException("Invalid password reset token.");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setPasswordHash(passwordHasher.hashPassword(request.getNewPassword()));
        userRepository.save(user);

        prt.setUsed(true);
        prt.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(prt);

        emailService.sendPasswordChangedEmail(user.getEmail(), user.getFullName());
    }

    public UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setEmployeeId(user.getEmployeeId());
        dto.setPhone(user.getPhone());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setTimeFormat(user.getTimeFormat());
        dto.setTimeZone(user.getTimeZone());
        dto.setLanguage(user.getLanguage());

        String roleName = user.getRole() != null ? user.getRole().getName() : "Receptionist";
        dto.setRoleName(roleName);
        dto.setRole(UserRole.fromString(roleName));
        return dto;
    }
}
