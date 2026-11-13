package com.careflow.hospitalmanagement.security;

import com.careflow.hospitalmanagement.domain.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Integer userId;
    private final String username;
    private final String email;
    private final String password;
    private final String roleName;
    private final boolean isActive;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Integer userId, String username, String email, String password, String roleName, boolean isActive, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.roleName = roleName;
        this.isActive = isActive;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "Receptionist";
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add role with ROLE_ prefix for Spring Security hasRole(...)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
        // Add bare role name as authority as well
        authorities.add(new SimpleGrantedAuthority(roleName));

        // Add permissions
        if (user.getRole() != null && user.getRole().getRolePermissions() != null) {
            user.getRole().getRolePermissions().forEach(rp -> {
                if (rp.getPermission() != null && rp.getPermission().getName() != null) {
                    authorities.add(new SimpleGrantedAuthority(rp.getPermission().getName()));
                }
            });
        }

        return new UserPrincipal(
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getPasswordHash(),
            roleName,
            user.isActive(),
            authorities
        );
    }

    public Integer getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
