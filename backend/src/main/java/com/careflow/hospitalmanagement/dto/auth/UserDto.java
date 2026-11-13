package com.careflow.hospitalmanagement.dto.auth;

import com.careflow.hospitalmanagement.common.RolePermissions;
import com.careflow.hospitalmanagement.common.TimeFormat;
import com.careflow.hospitalmanagement.domain.enums.UserRole;
import java.time.LocalDateTime;
import java.util.List;

public class UserDto {

    private int userId;
    private String username = "";
    private String email = "";
    private String fullName = "";
    private String employeeId;
    private String phone;
    private UserRole role = UserRole.Receptionist;
    private String roleName = "Receptionist";
    private int hierarchyLevel = 40;
    private List<String> permissions;
    private LocalDateTime lastLoginAt;
    private TimeFormat timeFormat = TimeFormat.Hour12;
    private String timeZone = "UTC";
    private String language = "en-US";

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
        if (role != null) {
            this.hierarchyLevel = role.getLevel();
            if (this.permissions == null || this.permissions.isEmpty()) {
                this.permissions = RolePermissions.getPermissions(role);
            }
        }
    }

    public String getRoleName() {
        return roleName != null && !roleName.isBlank() ? roleName : (role != null ? role.name() : "");
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public List<String> getPermissions() {
        if (permissions == null && role != null) {
            permissions = RolePermissions.getPermissions(role);
        }
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
