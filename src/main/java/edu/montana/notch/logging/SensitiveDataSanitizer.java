package edu.montana.notch.logging;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveDataSanitizer {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "passwd", "pwd", "pass",
            "token", "secret", "apikey", "api_key", "api-key",
            "creditcard", "credit_card", "credit-card", "cc", "cvv", "ssn",
            "session", "cookie", "authorization", "auth"
    );

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(password|passwd|pwd|pass|token|secret|apikey|api_key|api-key|" +
                    "creditcard|credit_card|credit-card|cc|cvv|ssn|session|cookie|authorization|auth)" +
                    "\\s*[=:]\\s*[^,\\s}\\]]+",
            Pattern.CASE_INSENSITIVE
    );

    private SensitiveDataSanitizer() {
    }

    public static String sanitize(String message) {
        if (message == null) {
            return null;
        }

        Matcher matcher = SENSITIVE_PATTERN.matcher(message);
        return matcher.replaceAll("$1=***REDACTED***");
    }

    public static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String lowerKey = key.toLowerCase();
        return SENSITIVE_KEYS.stream().anyMatch(lowerKey::contains);
    }
}