package edu.montana.notch.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class BetterRandom {
    private BetterRandom() {
    }

    private static final SecureRandom random;

    static {
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private static final String PRINTABLE_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String secureString(int n) {
        return random
                .ints(n, 0, PRINTABLE_CHARS.length())
                .mapToObj(PRINTABLE_CHARS::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    public static byte[] secureBytes(int n) {
        byte[] salt = new byte[n];
        random.nextBytes(salt);
        return salt;
    }
}
