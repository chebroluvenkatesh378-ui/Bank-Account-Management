package com.bank.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class PasswordUtil {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { }

    public static String hash(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password.toCharArray(), salt);
        return ITERATIONS + ":" + HexFormat.of().formatHex(salt) + ":" + HexFormat.of().formatHex(hash);
    }

    public static boolean verify(String password, String encodedHash) {
        try {
            String[] parts = encodedHash.split(":", 3);
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = HexFormat.of().parseHex(parts[1]);
            byte[] expected = HexFormat.of().parseHex(parts[2]);
            byte[] actual = derive(password.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt) {
        return derive(password, salt, ITERATIONS);
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Password hashing is unavailable.", exception);
        } finally {
            spec.clearPassword();
        }
    }
}