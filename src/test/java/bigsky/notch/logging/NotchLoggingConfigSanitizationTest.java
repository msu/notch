package bigsky.notch.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

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

        assertTrue(message.contains("password=***REDACTED***"));
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

        assertTrue(message.contains("password=secret123"));
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

        assertTrue(message.contains("password=***REDACTED***"));
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

        assertTrue(message.contains("password=***REDACTED***"));
        assertTrue(message.contains("username=[john]"));
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

        assertTrue(message.contains("password=***REDACTED***"));
        assertTrue(message.contains("token=***REDACTED***"));
        assertTrue(message.contains("apikey=***REDACTED***"));
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

        assertTrue(message.contains("username=john"));
        assertTrue(message.contains("email=john@example.com"));
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

        assertTrue(message.contains("INFO"));
        assertTrue(message.contains("bigsky.jackknife.TestLogger"));
        assertTrue(message.contains("password=***REDACTED***"));
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

        assertTrue(message.contains("Simple message"));
        assertFalse(message.contains("***REDACTED***"));
    }
}