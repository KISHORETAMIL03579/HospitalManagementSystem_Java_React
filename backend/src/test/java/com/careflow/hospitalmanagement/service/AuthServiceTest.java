package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.*;
import com.careflow.hospitalmanagement.domain.enums.RegistrationStatus;
import com.careflow.hospitalmanagement.dto.auth.*;
import com.careflow.hospitalmanagement.exception.AccountInactiveException;
import com.careflow.hospitalmanagement.exception.AccountPendingApprovalException;
import com.careflow.hospitalmanagement.exception.AccountRejectedException;
import com.careflow.hospitalmanagement.repository.*;
import com.careflow.hospitalmanagement.security.JwtTokenProvider;
import com.careflow.hospitalmanagement.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private InvitationCodeRepository invitationCodeRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private StaffRegistrationRequestRepository staffRegistrationRequestRepository;

    @Spy
    private PasswordHasher passwordHasher = new PasswordHasher();

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        sampleRole = new Role();
        sampleRole.setRoleId(1);
        sampleRole.setName("Doctor");
        sampleRole.setLevel(30);

        sampleUser = new User();
        sampleUser.setUserId(10);
        sampleUser.setUsername("dr.smith");
        sampleUser.setEmail("dr.smith@careflow.com");
        sampleUser.setFullName("Dr. Smith");
        sampleUser.setPasswordHash(passwordHasher.hashPassword("Password123!"));
        sampleUser.setActive(true);
        sampleUser.setRoleId(1);
        sampleUser.setRole(sampleRole);
    }

    @Test
    @DisplayName("login should succeed with valid credentials")
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("dr.smith");
        req.setPassword("Password123!");

        when(userRepository.findByEmailOrUsernameWithRole("dr.smith"))
                .thenReturn(Optional.of(sampleUser));
        when(jwtTokenProvider.generateToken(sampleUser)).thenReturn("jwt_mock_token");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refresh_mock_token");

        AuthResponse response = authService.login(req);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt_mock_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_mock_token");
        assertThat(response.getUser().getUsername()).isEqualTo("dr.smith");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("login should fail if user not found")
    void testLoginUserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("nonexistent");
        req.setPassword("Password123!");

        when(userRepository.findByEmailOrUsernameWithRole("nonexistent"))
                .thenReturn(Optional.empty());
        when(staffRegistrationRequestRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc("nonexistent"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not registered in CareFlow HMS");
    }

    @Test
    @DisplayName("login should throw AccountPendingApprovalException if staff registration is pending")
    void testLoginStaffPending() {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("pending_user");
        req.setPassword("Password123!");

        when(userRepository.findByEmailOrUsernameWithRole("pending_user"))
                .thenReturn(Optional.empty());

        StaffRegistrationRequest reg = new StaffRegistrationRequest();
        reg.setStatus(RegistrationStatus.Pending);
        when(staffRegistrationRequestRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc("pending_user"))
                .thenReturn(Optional.of(reg));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AccountPendingApprovalException.class);
    }

    @Test
    @DisplayName("login should throw AccountRejectedException if staff registration is rejected")
    void testLoginStaffRejected() {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("rejected_user");
        req.setPassword("Password123!");

        when(userRepository.findByEmailOrUsernameWithRole("rejected_user"))
                .thenReturn(Optional.empty());

        StaffRegistrationRequest reg = new StaffRegistrationRequest();
        reg.setStatus(RegistrationStatus.Rejected);
        when(staffRegistrationRequestRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc("rejected_user"))
                .thenReturn(Optional.of(reg));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AccountRejectedException.class);
    }

    @Test
    @DisplayName("login should throw BadCredentialsException on password mismatch")
    void testLoginBadPassword() {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("dr.smith");
        req.setPassword("WrongPassword");

        when(userRepository.findByEmailOrUsernameWithRole("dr.smith"))
                .thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Incorrect password provided");
    }

    @Test
    @DisplayName("login should throw AccountInactiveException if user is inactive")
    void testLoginInactiveUser() {
        sampleUser.setActive(false);
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("dr.smith");
        req.setPassword("Password123!");

        when(userRepository.findByEmailOrUsernameWithRole("dr.smith"))
                .thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AccountInactiveException.class);
    }

    @Test
    @DisplayName("register should succeed with valid invitation code")
    void testRegisterWithInvitation() {
        RegisterUserRequest req = new RegisterUserRequest();
        req.setUsername("new.nurse");
        req.setEmail("nurse@careflow.com");
        req.setPassword("SecurePass123!");
        req.setConfirmPassword("SecurePass123!");
        req.setFullName("Nurse Jane");
        req.setInvitationCode("INV-TEST-CODE");

        when(userRepository.existsByUsernameIgnoreCase("new.nurse")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("nurse@careflow.com")).thenReturn(false);

        InvitationCode inv = new InvitationCode();
        inv.setCode("INV-TEST-CODE");
        inv.setUsed(false);
        inv.setExpiresAt(LocalDateTime.now().plusDays(1));
        inv.setTargetRole(sampleRole);

        when(invitationCodeRepository.findByCode("INV-TEST-CODE")).thenReturn(Optional.of(inv));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId(99);
            return u;
        });
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt_mock");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refresh_mock");

        AuthResponse resp = authService.register(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getToken()).isEqualTo("jwt_mock");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register should throw IllegalArgumentException if username exists")
    void testRegisterDuplicateUsername() {
        RegisterUserRequest req = new RegisterUserRequest();
        req.setUsername("existing");
        req.setEmail("test@careflow.com");
        req.setPassword("SecurePass123!");
        req.setConfirmPassword("SecurePass123!");
        req.setInvitationCode("INV-123");

        when(userRepository.existsByUsernameIgnoreCase("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }
}
