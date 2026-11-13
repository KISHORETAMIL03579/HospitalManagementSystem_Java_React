package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.*;
import com.careflow.hospitalmanagement.domain.enums.RegistrationStatus;
import com.careflow.hospitalmanagement.dto.staff.*;
import com.careflow.hospitalmanagement.repository.*;
import com.careflow.hospitalmanagement.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffRegistrationServiceTest {

    @Mock
    private StaffRegistrationRequestRepository staffRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private DepartmentRepository deptRepo;

    @Mock
    private InvitationCodeRepository invitationRepo;

    @Mock
    private EmailLogRepository emailLogRepo;

    @org.mockito.Spy
    private PasswordHasher passwordHasher = new PasswordHasher();

    @Mock
    private EmailService emailService;

    @InjectMocks
    private StaffRegistrationService staffService;

    private Role nurseRole;

    @BeforeEach
    void setUp() {
        nurseRole = new Role();
        nurseRole.setRoleId(2);
        nurseRole.setName("Nurse");
    }

    @Test
    @DisplayName("submitStaffRegistrationRequest succeeds for new applicant")
    void testSubmitSuccess() {
        SubmitStaffRegistrationRequest req = new SubmitStaffRegistrationRequest();
        req.setFullName("Jane Nurse");
        req.setEmail("nurse.jane@careflow.com");
        req.setUsername("jane.nurse");
        req.setPassword("Password123!");
        req.setEmployeeId("EMP101");
        req.setDepartmentId(1);
        req.setRequestedRoleId(2);

        when(userRepo.existsByEmailIgnoreCase("nurse.jane@careflow.com")).thenReturn(false);
        when(userRepo.existsByUsernameIgnoreCase("jane.nurse")).thenReturn(false);
        when(staffRepo.existsPendingByEmail("nurse.jane@careflow.com")).thenReturn(false);
        when(roleRepo.findById(2)).thenReturn(Optional.of(nurseRole));

        StaffRegistrationRequest saved = new StaffRegistrationRequest();
        saved.setStaffRegistrationRequestId(10);
        saved.setEmail("nurse.jane@careflow.com");
        saved.setFullName("Jane Nurse");
        saved.setStatus(RegistrationStatus.Pending);
        saved.setRequestedRoleId(2);
        saved.setRequestedRole(nurseRole);

        when(staffRepo.save(any(StaffRegistrationRequest.class))).thenReturn(saved);

        StaffRegistrationRequestDto dto = staffService.submitStaffRegistrationRequest(req);

        assertThat(dto).isNotNull();
        assertThat(dto.getEmail()).isEqualTo("nurse.jane@careflow.com");
        verify(emailService).sendRegistrationConfirmationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("submitStaffRegistrationRequest throws if email already exists")
    void testSubmitDuplicateEmail() {
        SubmitStaffRegistrationRequest req = new SubmitStaffRegistrationRequest();
        req.setEmail("existing@careflow.com");
        req.setUsername("jane.nurse");

        when(userRepo.existsByEmailIgnoreCase("existing@careflow.com")).thenReturn(true);

        assertThatThrownBy(() -> staffService.submitStaffRegistrationRequest(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("approveStaffRegistrationRequest throws if already approved")
    void testApproveAlreadyApproved() {
        StaffRegistrationRequest existing = new StaffRegistrationRequest();
        existing.setStaffRegistrationRequestId(5);
        existing.setStatus(RegistrationStatus.Approved);

        when(staffRepo.findByIdWithDetails(5)).thenReturn(Optional.of(existing));

        ApproveStaffRequest approveReq = new ApproveStaffRequest();
        approveReq.setNotes("All clear");

        assertThatThrownBy(() -> staffService.approveStaffRegistrationRequest(5, 1, approveReq))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been approved");
    }
}
