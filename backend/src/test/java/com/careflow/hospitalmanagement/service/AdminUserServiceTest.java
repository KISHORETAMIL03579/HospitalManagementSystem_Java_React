package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.Role;
import com.careflow.hospitalmanagement.domain.entities.User;
import com.careflow.hospitalmanagement.dto.admin.*;
import com.careflow.hospitalmanagement.repository.RoleRepository;
import com.careflow.hospitalmanagement.repository.UserRepository;
import com.careflow.hospitalmanagement.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private com.careflow.hospitalmanagement.repository.InvitationCodeRepository invitationCodeRepository;

    @Spy
    private PasswordHasher passwordHasher = new PasswordHasher();

    @InjectMocks
    private AdminUserService adminUserService;

    private User sampleUser;
    private Role sampleRole;
    private Integer sampleUserId = 10;
    private Integer sampleRoleId = 2;

    @BeforeEach
    void setUp() {
        sampleRole = new Role();
        sampleRole.setRoleId(sampleRoleId);
        sampleRole.setName("Doctor");
        sampleRole.setLevel(30);

        sampleUser = new User();
        sampleUser.setUserId(sampleUserId);
        sampleUser.setUsername("test.doctor");
        sampleUser.setEmail("test.doctor@careflow.com");
        sampleUser.setFullName("Test Doctor");
        sampleUser.setActive(true);
        sampleUser.setRoleId(sampleRoleId);
        sampleUser.setRole(sampleRole);
    }

    @Test
    @DisplayName("getAllUsersForAdmin returns matching list")
    void testGetAllUsers() {
        when(userRepository.searchUsers(null, null, null)).thenReturn(List.of(sampleUser));

        List<AdminUserDto> result = adminUserService.getAllUsersForAdmin(null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("test.doctor");
    }

    @Test
    @DisplayName("createUserByAdmin succeeds when valid")
    void testCreateUserSuccess() {
        CreateUserByAdminRequest req = new CreateUserByAdminRequest();
        req.setUsername("new.user");
        req.setEmail("new@careflow.com");
        req.setPassword("SecretPass123!");
        req.setFullName("New User");
        req.setPhone("+1234567890");
        req.setRoleId(sampleRoleId);

        when(userRepository.existsByUsernameIgnoreCase("new.user")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("new@careflow.com")).thenReturn(false);
        when(roleRepository.findById(sampleRoleId)).thenReturn(Optional.of(sampleRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId(100);
            return u;
        });

        AdminUserDto dto = adminUserService.createUserByAdmin(req);

        assertThat(dto).isNotNull();
        assertThat(dto.getUsername()).isEqualTo("new.user");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("toggleUserStatusByAdmin updates user active state")
    void testUpdateUserStatus() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);

        AdminUserDto dto = adminUserService.toggleUserStatusByAdmin(sampleUserId, 1, false);

        assertThat(dto).isNotNull();
        assertThat(sampleUser.isActive()).isFalse();
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("updateUserRoleByAdmin updates user role")
    void testUpdateUserRole() {
        Integer newRoleId = 3;
        Role newRole = new Role();
        newRole.setRoleId(newRoleId);
        newRole.setName("Nurse");
        newRole.setLevel(35);

        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));
        when(roleRepository.findById(newRoleId)).thenReturn(Optional.of(newRole));
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);

        AdminUserDto dto = adminUserService.updateUserRoleByAdmin(sampleUserId, newRoleId);

        assertThat(dto).isNotNull();
        assertThat(sampleUser.getRole().getName()).isEqualTo("Nurse");
    }
}
