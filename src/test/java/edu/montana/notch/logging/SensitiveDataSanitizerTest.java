package edu.montana.notch.logging;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

class SensitiveDataSanitizerTest {

    @Test
    void testSanitizePassword() {
        String input = "user login with password=secret123";
        String result = SensitiveDataSanitizer.sanitize(input);
        assertEquals("user login with password=***REDACTED***", result);
    }

    @Test
    void testSanitizeToken() {
        String input = "Bearer token=abc123xyz";
        String result = SensitiveDataSanitizer.sanitize(input);
        assertEquals("Bearer token=***REDACTED***", result);
    }

    @Test
    void testSanitizeApiKey() {
        String input = "config api_key=sk_test_12345";
        String result = SensitiveDataSanitizer.sanitize(input);
        assertEquals("config api_key=***REDACTED***", result);
    }

    @Test
    void testSanitizeCreditCard() {
        String input = "payment creditcard=4111111111111111";
        String result = SensitiveDataSanitizer.sanitize(input);
        assertEquals("payment creditcard=***REDACTED***", result);
    }

    @Test
    void testSanitizeMultipleFields() {
        String input = "user=john password=secret123 token=abc123";
        String result = SensitiveDataSanitizer.sanitize(input);
        assertContains("password=***REDACTED***", result);
        assertContains("token=***REDACTED***", result);
        assertContains("user=john", result);
    }

    @Test
    void testSanitizeWithColonSeparator() {
        String input = "credentials password: secret123";
        String result = SensitiveDataSanitizer.sanitize(input);
        assertEquals("credentials password=***REDACTED***", result);
    }

    @Test
    void testSanitizeFormParamMap() {
        String input = "Form values: {password=[secret123], username=[john]}";
        String result = SensitiveDataSanitizer.sanitize(input);
        assertContains("password=***REDACTED***", result);
        assertContains("username=[john]", result);
    }

    @Test
    void testSanitizeCaseInsensitive() {
        String input1 = "PASSWORD=secret";
        String input2 = "Password=secret";
        String input3 = "password=secret";

        assertEquals("PASSWORD=***REDACTED***", SensitiveDataSanitizer.sanitize(input1));
        assertEquals("Password=***REDACTED***", SensitiveDataSanitizer.sanitize(input2));
        assertEquals("password=***REDACTED***", SensitiveDataSanitizer.sanitize(input3));
    }

    @Test
    void testSanitizeWithCommaDelimitedValues() {
        String input = "data password=secret,username=john,token=xyz";
        String result = SensitiveDataSanitizer.sanitize(input);
        assertContains("password=***REDACTED***", result);
        assertContains("token=***REDACTED***", result);
        assertContains("username=john", result);
    }

    @Test
    void testSanitizeNull() {
        assertNull(SensitiveDataSanitizer.sanitize(null));
    }

    @Test
    void testSanitizeEmptyString() {
        assertEquals("", SensitiveDataSanitizer.sanitize(""));
    }

    @Test
    void testSanitizeNoSensitiveData() {
        String input = "user=john age=30 city=Boston";
        String result = SensitiveDataSanitizer.sanitize(input);
        assertEquals(input, result);
    }

    @Test
    void testIsSensitiveKeyPassword() {
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("password"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("PASSWORD"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("user_password"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("myPassword"));
    }

    @Test
    void testIsSensitiveKeyToken() {
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("token"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("auth_token"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("csrf_token"));
    }

    @Test
    void testIsSensitiveKeyApiKey() {
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("apikey"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("api_key"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("api-key"));
    }

    @Test
    void testIsSensitiveKeyCreditCard() {
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("creditcard"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("credit_card"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("cc"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("cvv"));
    }

    @Test
    void testIsSensitiveKeySession() {
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("session"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("session_id"));
        assertTrue(SensitiveDataSanitizer.isSensitiveKey("cookie"));
    }

    @Test
    void testIsSensitiveKeyNotSensitive() {
        assertFalse(SensitiveDataSanitizer.isSensitiveKey("username"));
        assertFalse(SensitiveDataSanitizer.isSensitiveKey("email"));
        assertFalse(SensitiveDataSanitizer.isSensitiveKey("age"));
        assertFalse(SensitiveDataSanitizer.isSensitiveKey("city"));
    }

    @Test
    void testIsSensitiveKeyNull() {
        assertFalse(SensitiveDataSanitizer.isSensitiveKey(null));
    }

    @Test
    void testSanitizeSecretVariations() {
        assertContains("***REDACTED***", SensitiveDataSanitizer.sanitize("secret=value"));
        assertContains("***REDACTED***", SensitiveDataSanitizer.sanitize("client_secret=value"));
        assertContains("***REDACTED***", SensitiveDataSanitizer.sanitize("api_secret=value"));
    }

    @Test
    void testSanitizeAuthorizationVariations() {
        assertContains("***REDACTED***", SensitiveDataSanitizer.sanitize("authorization=bearer xyz"));
        assertContains("***REDACTED***", SensitiveDataSanitizer.sanitize("auth=basic abc"));
    }

    @Test
    void testSanitizeSSN() {
        String input = "employee ssn=123-45-6789";
        String result = SensitiveDataSanitizer.sanitize(input);
        assertContains("ssn=***REDACTED***", result);
    }

    @Test
    void testSanitizeRealWorldFormData() {
        String input = "Form values: {username=[alice], password=[p@ssw0rd!], email=[alice@example.com], remember_me=[true]}";
        String result = SensitiveDataSanitizer.sanitize(input);

        assertContains("username=[alice]", result);
        assertContains("password=***REDACTED***", result);
        assertContains("email=[alice@example.com]", result);
        assertContains("remember_me=[true]", result);
    }
}