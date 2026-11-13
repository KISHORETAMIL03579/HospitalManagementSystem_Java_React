package com.careflow.hospitalmanagement.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;

@Component
public class PasswordHasher implements PasswordEncoder {

    private static final int SALT_SIZE = 16;      // 128 bit
    private static final int KEY_SIZE_BITS = 256;  // 256 bit = 32 bytes
    private static final int ITERATIONS = 100000;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private final SecureRandom secureRandom = new SecureRandom();
    private final HexFormat hexFormat = HexFormat.of().withUpperCase();

    public String hashPassword(String password) {
        return encode(password);
    }

    public boolean verifyPassword(String password, String passwordHash) {
        return matches(password, passwordHash);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }

        byte[] salt = new byte[SALT_SIZE];
        secureRandom.nextBytes(salt);

        byte[] hash = pbkdf2(rawPassword.toString().toCharArray(), salt, ITERATIONS, KEY_SIZE_BITS);
        return hexFormat.formatHex(salt) + "-" + hexFormat.formatHex(hash);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }

        try {
            String[] parts = encodedPassword.split("-");
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = hexFormat.parseHex(parts[0]);
            byte[] expectedHash = hexFormat.parseHex(parts[1]);

            byte[] actualHash = pbkdf2(rawPassword.toString().toCharArray(), salt, ITERATIONS, KEY_SIZE_BITS);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Error executing PBKDF2 algorithm: " + e.getMessage(), e);
        }
    }
}
