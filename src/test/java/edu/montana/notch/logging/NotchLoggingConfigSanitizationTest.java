package edu.montana.notch.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

class NotchLoggingConfigSanitizationTest {

    private NotchLoggingConfig config;

    @BeforeEach
    void setUp() {
        config = new NotchLoggingConfig();
    }

    @Test
    void testSanitizationEnabledByDefault() {
        String message = config.formatMessage(
                "TestLogger",
                Level.INFO,
                null,
                "User login: password={}",
                new Object[]{"secret123"},
                null
        );

        assertContains("password=***REDACTED***", message);
        assertFalse(message.contains("secret123"));
    }

    @Test
    void testSanitizationCanBeDisabled() {
        config.disableSanitization();

        String message = config.formatMessage(
                "TestLogger",
                Level.INFO,
                null,
                "User login: password={}",
                new Object[]{"secret123"},
                null
        );

        assertContains("password=secret123", message);
        assertFalse(message.contains("***REDACTED***"));
    }

    @Test
    void testSanitizationCanBeReEnabled() {
        config.disableSanitization();
        config.enableSanitization();

        String message = config.formatMessage(
                "TestLogger",
                Level.INFO,
                null,
                "User login: password={}",
                new Object[]{"secret123"},
                null
        );

        assertContains("password=***REDACTED***", message);
        assertFalse(message.contains("secret123"));
    }

    @Test
    void testSanitizeFormParamMapLogging() {
        String message = config.formatMessage(
                "TestLogger",
                Level.INFO,
                null,
                "Form values: {}",
                new Object[]{"{password=[secret], username=[john]}"},
                null
        );

        assertContains("password=***REDACTED***", message);
        assertContains("username=[john]", message);
        assertFalse(message.contains("secret"));
    }

    @Test
    void testSanitizeMultipleSensitiveFields() {
        String message = config.formatMessage(
                "TestLogger",
                Level.INFO,
                null,
                "Auth data: password={} token={} apikey={}",
                new Object[]{"pass123", "tok456", "key789"},
                null
        );

        assertContains("password=***REDACTED***", message);
        assertContains("token=***REDACTED***", message);
        assertContains("apikey=***REDACTED***", message);
        assertFalse(message.contains("pass123"));
        assertFalse(message.contains("tok456"));
        assertFalse(message.contains("key789"));
    }

    @Test
    void testNonSensitiveDataNotSanitized() {
        String message = config.formatMessage(
                "TestLogger",
                Level.INFO,
                null,
                "User data: username={} email={}",
                new Object[]{"john", "john@example.com"},
                null
        );

        assertContains("username=john", message);
        assertContains("email=john@example.com", message);
        assertFalse(message.contains("***REDACTED***"));
    }

    @Test
    void testSanitizationWithDifferentLogLevels() {
        for (Level level : new Level[]{Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR}) {
            String message = config.formatMessage(
                    "TestLogger",
                    level,
                    null,
                    "password={}",
                    new Object[]{"secret"},
                    null
            );

            assertTrue(message.contains("password=***REDACTED***"),
                    "Sanitization should work at level " + level);
            assertFalse(message.contains("secret"),
                    "Secret should be redacted at level " + level);
        }
    }

    @Test
    void testSanitizationPreservesLogFormat() {
        config.showTimestamp();
        config.showFullLoggerName();

        String message = config.formatMessage(
                "bigsky.jackknife.TestLogger",
                Level.INFO,
                null,
                "password={}",
                new Object[]{"secret"},
                null
        );

        assertContains("INFO", message);
        assertContains("bigsky.jackknife.TestLogger", message);
        assertContains("password=***REDACTED***", message);
    }

    @Test
    void testSanitizationWithEmptyMessage() {
        String message = config.formatMessage(
                "TestLogger",
                Level.INFO,
                null,
                "",
                new Object[]{},
                null
        );

        assertNotNull(message);
        assertFalse(message.contains("***REDACTED***"));
    }

    @Test
    void testSanitizationWithEmptyArguments() {
        String message = config.formatMessage(
                "TestLogger",
                Level.INFO,
                null,
                "Simple message",
                new Object[]{},
                null
        );

        assertContains("Simple message", message);
        assertFalse(message.contains("***REDACTED***"));
    }
}