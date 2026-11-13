package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.InvitationCode;
import com.careflow.hospitalmanagement.domain.entities.Role;
import com.careflow.hospitalmanagement.domain.entities.User;
import com.careflow.hospitalmanagement.domain.enums.UserRole;
import com.careflow.hospitalmanagement.dto.admin.*;
import com.careflow.hospitalmanagement.exception.ResourceNotFoundException;
import com.careflow.hospitalmanagement.repository.InvitationCodeRepository;
import com.careflow.hospitalmanagement.repository.RoleRepository;
import com.careflow.hospitalmanagement.repository.UserRepository;
import com.careflow.hospitalmanagement.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InvitationCodeRepository invitationCodeRepository;
    private final PasswordHasher passwordHasher;

    public AdminUserService(UserRepository userRepository, RoleRepository roleRepository, InvitationCodeRepository invitationCodeRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.invitationCodeRepository = invitationCodeRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional(readOnly = true)
    public List<AdminUserDto> getAllUsersForAdmin(String search, Integer roleId, Boolean isActive) {
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        List<User> users = userRepository.searchUsers(cleanSearch, roleId, isActive);
        List<AdminUserDto> dtos = new ArrayList<>();
        for (User u : users) {
            dtos.add(mapToAdminUserDto(u));
        }
        return dtos;
    }

    public AdminUserDto createUserByAdmin(CreateUserByAdminRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered.");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Selected role could not be resolved."));

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordHasher.hashPassword(request.getPassword()));
        newUser.setFullName(request.getFullName().trim());
        newUser.setEmployeeId(request.getEmployeeId() != null && !request.getEmployeeId().isBlank() ? request.getEmployeeId().trim() : null);
        newUser.setPhone(normalizePhone(request.getPhone()));
        newUser.setRoleId(role.getRoleId());
        newUser.setRole(role);
        newUser.setActive(true);

        User saved = userRepository.save(newUser);
        log.info("ADMIN ACTION: Admin created user account '{}' with role '{}'.", saved.getUsername(), role.getName());

        return mapToAdminUserDto(saved);
    }

    public AdminUserDto updateUserByAdmin(int userId, UpdateUserByAdminRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account #" + userId + " not found."));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmailIgnoreCase(newEmail)) {
                    throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already in use by another user.");
                }
                user.setEmail(newEmail);
            }
        }

        if (request.getRoleId() > 0 && (user.getRoleId() == null || user.getRoleId() != request.getRoleId())) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected role is invalid."));
            user.setRoleId(role.getRoleId());
            user.setRole(role);
        }

        if (request.getEmployeeId() != null) {
            user.setEmployeeId(!request.getEmployeeId().isBlank() ? request.getEmployeeId().trim() : null);
        }

        if (request.getPhone() != null) {
            user.setPhone(normalizePhone(request.getPhone()));
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordHasher.hashPassword(request.getPassword().trim()));
        }

        User updated = userRepository.save(user);
        log.info("ADMIN ACTION: Admin updated details for user account #{} ('{}').", userId, updated.getUsername());

        return mapToAdminUserDto(updated);
    }

    public AdminUserDto updateUserRoleByAdmin(int userId, int newRoleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account #" + userId + " not found."));

        Role role = roleRepository.findById(newRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Selected role is invalid."));

        user.setRoleId(role.getRoleId());
        user.setRole(role);
        User updated = userRepository.save(user);

        log.info("ADMIN ACTION: Admin updated role for user #{} to '{}'.", userId, role.getName());
        return mapToAdminUserDto(updated);
    }

    public AdminUserDto toggleUserStatusByAdmin(int targetUserId, int currentAdminId, boolean isActive) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User account #" + targetUserId + " not found."));

        if (targetUserId == currentAdminId && !isActive) {
            throw new IllegalArgumentException("Administrators are prohibited from deactivating their own account.");
        }

        if (!isActive && user.getRole() != null && "Admin".equalsIgnoreCase(user.getRole().getName())) {
            long activeAdminCount = userRepository.countActiveAdmins();
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Cannot deactivate the last active administrator account in the system.");
            }
        }

        user.setActive(isActive);
        if (!isActive) {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiryTime(null);
        }

        User updated = userRepository.save(user);
        log.info("ADMIN ACTION: Admin #{} changed status of user #{} to IsActive={}.", currentAdminId, targetUserId, isActive);

        return mapToAdminUserDto(updated);
    }

    public void deleteUserByAdmin(int targetUserId, int currentAdminId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User account #" + targetUserId + " not found."));

        if (targetUserId == currentAdminId) {
            throw new IllegalArgumentException("Administrators are prohibited from removing their own account.");
        }

        if (user.getRole() != null && "Admin".equalsIgnoreCase(user.getRole().getName())) {
            long activeAdminCount = userRepository.countActiveAdmins();
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Cannot remove the last active administrator account in the system.");
            }
        }

        // Soft delete
        user.setActive(false);
        user.setRefreshToken(null);
        user.setRefreshTokenExpiryTime(null);
        userRepository.save(user);

        log.info("ADMIN ACTION: Admin #{} soft-deleted user #{} ('{}'). Medical records preserved.",
                currentAdminId, targetUserId, user.getEmail());
    }

    private AdminUserDto mapToAdminUserDto(User u) {
        AdminUserDto dto = new AdminUserDto();
        dto.setUserId(u.getUserId());
        dto.setFullName(u.getFullName());
        dto.setEmail(u.getEmail());
        dto.setUsername(u.getUsername());
        dto.setEmployeeId(u.getEmployeeId());
        dto.setPhone(u.getPhone());
        dto.setRoleId(u.getRoleId() != null ? u.getRoleId() : 0);
        String rName = u.getRole() != null ? u.getRole().getName() : "Staff";
        dto.setRoleName(rName);
        dto.setRoleEnum(UserRole.fromString(rName));
        dto.setActive(u.isActive());
        dto.setLastLoginAt(u.getLastLoginAt());
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }

    private String normalizePhone(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String trimmed = raw.trim();
        String digits = trimmed.replaceAll("\\D", "");
        if (digits.length() < 7 || digits.length() > 15) {
            throw new IllegalArgumentException("Phone number must contain between 7 and 15 digits in valid international E.164 format (e.g. +919876543210).");
        }
        return trimmed.startsWith("+") ? ("+" + digits) : (digits.length() > 10 ? ("+" + digits) : trimmed);
    }

    @Transactional(readOnly = true)
    public List<InvitationCodeDto> getAllInvitationCodes() {
        return invitationCodeRepository.findAllWithRole().stream()
                .map(this::mapToInvitationCodeDto)
                .toList();
    }

    public InvitationCodeDto createInvitationCode(CreateInvitationCodeRequest request) {
        Role targetRole = roleRepository.findById(request.getTargetRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Target role not found with ID: " + request.getTargetRoleId()));

        String code = request.getCode();
        if (code == null || code.isBlank()) {
            code = targetRole.getName().toUpperCase() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else {
            code = code.trim().toUpperCase();
            if (invitationCodeRepository.existsByCode(code)) {
                throw new IllegalArgumentException("Invitation code '" + code + "' already exists.");
            }
        }

        InvitationCode inv = new InvitationCode();
        inv.setCode(code);
        inv.setCodeHash(passwordHasher.hashPassword(code));
        inv.setTargetRoleId(targetRole.getRoleId());
        inv.setTargetRole(targetRole);
        inv.setBoundEmail(request.getBoundEmail() != null && !request.getBoundEmail().isBlank() ? request.getBoundEmail().trim().toLowerCase() : null);
        inv.setBoundEmployeeId(request.getBoundEmployeeId() != null && !request.getBoundEmployeeId().isBlank() ? request.getBoundEmployeeId().trim() : null);
        inv.setBoundHospitalId(request.getBoundHospitalId() != null && !request.getBoundHospitalId().isBlank() ? request.getBoundHospitalId().trim() : null);
        inv.setExpiresAt(LocalDateTime.now().plusDays(request.getExpiresInDays()));
        inv.setUsed(false);

        InvitationCode saved = invitationCodeRepository.save(inv);
        log.info("SECURITY AUDIT: Admin created invitation code '{}' for role '{}'", code, targetRole.getName());
        return mapToInvitationCodeDto(saved);
    }

    private InvitationCodeDto mapToInvitationCodeDto(InvitationCode inv) {
        InvitationCodeDto dto = new InvitationCodeDto();
        dto.setInvitationCodeId(inv.getInvitationCodeId());
        dto.setCode(inv.getCode());
        dto.setTargetRoleId(inv.getTargetRoleId());
        dto.setTargetRoleName(inv.getTargetRole() != null ? inv.getTargetRole().getName() : "");
        dto.setBoundHospitalId(inv.getBoundHospitalId());
        dto.setBoundEmail(inv.getBoundEmail());
        dto.setBoundEmployeeId(inv.getBoundEmployeeId());
        dto.setUsed(inv.isUsed());
        dto.setUsedAt(inv.getUsedAt());
        dto.setUsedByUserId(inv.getUsedByUserId());
        dto.setExpiresAt(inv.getExpiresAt());
        dto.setCreatedAt(inv.getCreatedAt());
        return dto;
    }
}
