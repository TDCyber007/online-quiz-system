package com.quizsystem.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Password handling for user accounts.
 *
 * <p>Passwords are never stored as plain text. A random salt is generated for every
 * account and the password is stored as a salted SHA-256 hash.</p>
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private PasswordUtil() {
    }

    /** Generates a new random salt. */
    public static String newSalt() {
        StringBuilder salt = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            salt.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return salt.toString();
    }

    /** Hashes a plain text password together with the given salt. */
    public static String hash(String plainPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((plainPassword + salt).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available on this system", e);
        }
    }

    /** Checks a plain text password against the stored hash and salt. */
    public static boolean matches(String plainPassword, String salt, String expectedHash) {
        if (plainPassword == null || salt == null || expectedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(plainPassword, salt).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }

    /** Basic strength check used when a user registers. */
    public static boolean isStrongEnough(String password) {
        return password != null && password.length() >= 6;
    }
}
