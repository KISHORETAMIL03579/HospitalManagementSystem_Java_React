package com.careflow.hospitalmanagement.security;

import com.careflow.hospitalmanagement.domain.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:CareFlow_Super_Secret_JWT_Signing_Key_2026_Minimum_32_Bytes!}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-minutes:120}")
    private int jwtExpirationMinutes;

    @Value("${app.jwt.issuer:CareFlowHMS}")
    private String jwtIssuer;

    @Value("${app.jwt.audience:CareFlowHMSClient}")
    private String jwtAudience;

    private final SecureRandom secureRandom = new SecureRandom();

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "Receptionist";
        int level = user.getRole() != null ? user.getRole().getLevel() : 40;

        List<String> permissions = new ArrayList<>();
        if (user.getRole() != null && user.getRole().getRolePermissions() != null) {
            user.getRole().getRolePermissions().forEach(rp -> {
                if (rp.getPermission() != null && rp.getPermission().getName() != null) {
                    permissions.add(rp.getPermission().getName());
                }
            });
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (long) jwtExpirationMinutes * 60 * 1000);

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .issuer(jwtIssuer)
                .audience().add(jwtAudience).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("nameid", user.getUserId().toString())
                .claim("name", user.getUsername())
                .claim("given_name", user.getFullName())
                .claim("role", roleName)
                .claim("HierarchyLevel", String.valueOf(level))
                .claim("Permission", permissions)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String sub = claims.getSubject();
        return Integer.parseInt(sub);
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public int getExpirationMinutes() {
        return jwtExpirationMinutes;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
