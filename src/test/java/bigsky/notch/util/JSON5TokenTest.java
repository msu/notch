package bigsky.notch.util;

import bigsky.notch.chisel.TokenStream;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.json5.*;
import bigsky.notch.json5.JSON5TokenTypeNumber.NumberValue;
import bigsky.notch.json5.JSON5TokenTypeString.StringValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for individual JSON5 token types
 */
class JSON5TokenTest {

    // ===== String Token Tests =====

    @Test
    void testDoubleQuotedString() {
        String json = "\"hello world\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testDoubleQuotedString", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("\"hello world\"", str.repr());
        assertEquals("hello world", str.value());
        assertTrue(str.isDoubleQuoted());
        assertFalse(str.isSingleQuoted());
    }

    @Test
    void testSingleQuotedString() {
        String json = "'hello world'";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testSingleQuotedString", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("'hello world'", str.repr());
        assertEquals("hello world", str.value());
        assertTrue(str.isSingleQuoted());
        assertFalse(str.isDoubleQuoted());
    }

    @Test
    void testStringWithEscapes() {
        String json = "\"\\n\\t\\r\\\"\\\\\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testStringWithEscapes", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("\"\\n\\t\\r\\\"\\\\\"", str.repr());
        assertEquals("\n\t\r\"\\", str.value());
    }

    @Test
    void testStringWithUnicodeEscape() {
        String json = "\"\\u0048\\u0065\\u006C\\u006C\\u006F\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testStringWithUnicodeEscape", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("\"\\u0048\\u0065\\u006C\\u006C\\u006F\"", str.repr());
        assertEquals("Hello", str.value());
    }

    @Test
    void testStringWithHexEscape() {
        String json = "\"\\x41\\x42\\x43\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testStringWithHexEscape", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("\"\\x41\\x42\\x43\"", str.repr());
        assertEquals("ABC", str.value());
    }

    @Test
    void testEmptyString() {
        String json = "\"\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testEmptyString", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("\"\"", str.repr());
        assertEquals("", str.value());
    }

    @Test
    void testUnterminatedString() {
        assertThrows(TokenizeException.class, () -> JSON5.tokenize("testUnterminatedString", "\"unterminated"));
    }

    @Test
    void testInvalidEscapeSequence() {
        assertThrows(TokenizeException.class, () -> JSON5.tokenize("testInvalidEscapeSequence", "\"invalid\\z\""));
    }

    // ===== Number Token Tests =====

    @Test
    void testDecimalInteger() {
        TokenStream stream = JSON5.tokenize("testDecimalInteger", "123");
        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("123", num.repr());
        assertTrue(num.isInteger());
        assertEquals(123L, num.integerValue());
        assertFalse(num.isNaN());
        assertFalse(num.isInfinity());
    }

    @Test
    void testDecimalWithFraction() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testDecimalWithFraction", "123.456"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("123.456", num.repr());
        assertEquals(123.456, num.decimalValue());
        assertFalse(num.isInteger());
    }

    @Test
    void testDecimalFractionOnly() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testDecimalFractionOnly", ".456"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals(".456", num.repr());
        assertEquals(0.456, num.decimalValue());
    }

    @Test
    void testDecimalWithExponent() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testDecimalWithExponent", "123e10"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("123e10", num.repr());
        assertEquals(123e10, num.decimalValue());
    }

    @Test
    void testNegativeNumber() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testNegativeNumber", "-123.456"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("-123.456", num.repr());
        assertEquals(-123.456, num.decimalValue());
        assertTrue(num.isNegative());
    }

    @Test
    void testPositiveNumber() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testPositiveNumber", "+123.456"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("+123.456", num.repr());
        assertEquals(123.456, num.decimalValue());
        assertFalse(num.isNegative());
    }

    @Test
    void testHexNumber() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testHexNumber", "0xDECAF"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("0xDECAF", num.repr());
        assertEquals(0xDECAF, num.decimalValue());
        assertTrue(num.isInteger());
        assertEquals(0xDECAFL, num.integerValue());
    }

    @Test
    void testNegativeHexNumber() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testNegativeHexNumber", "-0xC0FFEE"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("-0xC0FFEE", num.repr());
        assertEquals(-0xC0FFEE, num.decimalValue());
        assertTrue(num.isNegative());
        assertTrue(num.isInteger());
        assertEquals(-0xC0FFEEL, num.integerValue());
    }

    @Test
    void testInfinity() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testInfinity", "Infinity"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("Infinity", num.repr());
        assertTrue(num.isInfinity());
        assertFalse(num.isNegative());
        assertTrue(Double.isInfinite(num.decimalValue()));
    }

    @Test
    void testNegativeInfinity() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testNegativeInfinity", "-Infinity"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("-Infinity", num.repr());
        assertTrue(num.isInfinity());
        assertTrue(num.isNegative());
        assertTrue(Double.isInfinite(num.decimalValue()));
    }

    @Test
    void testNaN() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testNaN", "NaN"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("NaN", num.repr());
        assertTrue(num.isNaN());
        assertFalse(num.isNegative());
    }

    @Test
    void testNegativeNaN() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testNegativeNaN", "-NaN"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("-NaN", num.repr());
        assertTrue(num.isNaN());
        assertTrue(num.isNegative());
    }

    @Test
    void testInvalidHexNoDigits() {
        assertThrows(TokenizeException.class, () -> JSON5.tokenize("testInvalidHexNoDigits", "0x"));
    }

    @Test
    void testInvalidExponentNoDigits() {
        assertThrows(TokenizeException.class, () -> JSON5.tokenize("testInvalidExponentNoDigits", "123e"));
    }

    // ===== Identifier Token Tests =====

    @Test
    void testIdentifier() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testIdentifier", "hello"));

        assertEquals(1, stream.size());
        assertEquals("hello", stream.take().str());
    }

    @Test
    void testIdentifierWithUnderscore() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testIdentifierWithUnderscore", "_private"));

        assertEquals(1, stream.size());
        assertEquals("_private", stream.take().str());
    }

    @Test
    void testIdentifierWithDollar() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testIdentifierWithDollar", "$special"));

        assertEquals(1, stream.size());
        assertEquals("$special", stream.take().str());
    }

    @Test
    void testKeywordTrue() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testKeywordTrue", "true"));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypeIdent.Keyword.JSON_TRUE, stream.take().type);
    }

    @Test
    void testKeywordFalse() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testKeywordFalse", "false"));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypeIdent.Keyword.JSON_FALSE, stream.take().type);
    }

    @Test
    void testKeywordNull() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testKeywordNull", "null"));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypeIdent.Keyword.JSON_NULL, stream.take().type);
    }

    @Test
    void testReservedKeyword() {
        assertThrows(TokenizeException.class, () -> JSON5.tokenize("testReservedKeyword", "class"));
        assertThrows(TokenizeException.class, () -> JSON5.tokenize("testReservedKeyword", "function"));
        assertThrows(TokenizeException.class, () -> JSON5.tokenize("testReservedKeyword", "const"));
    }

    // ===== Punctuation Token Tests =====

    @Test
    void testLeftBrace() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testLeftBrace", "{"));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypePunct.LeftBrace, stream.take().type);
    }

    @Test
    void testRightBrace() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testRightBrace", "}"));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypePunct.RightBrace, stream.take().type);
    }

    @Test
    void testLeftBracket() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testLeftBracket", "["));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypePunct.LeftBracket, stream.take().type);
    }

    @Test
    void testRightBracket() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testRightBracket", "]"));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypePunct.RightBracket, stream.take().type);
    }

    @Test
    void testColon() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testColon", ":"));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypePunct.Colon, stream.take().type);
    }

    @Test
    void testComma() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testComma", ","));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypePunct.Comma, stream.take().type);
    }

    // ===== Comment Token Tests =====

    @Test
    void testSingleLineComment() {
        String json = "// this is a comment\n";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testSingleLineComment", json));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypeComment.JSON5_COMMENT, stream.take().type);
    }

    @Test
    void testSingleLineCommentWithoutNewline() {
        String json = "// this is a comment at EOF";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testSingleLineCommentWithoutNewline", json));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypeComment.JSON5_COMMENT, stream.take().type);
    }

    @Test
    void testBlockComment() {
        String json = "/* this is a block comment */";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testBlockComment", json));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypeComment.JSON5_COMMENT, stream.take().type);
    }

    @Test
    void testMultiLineBlockComment() {
        String json = """
            /* this is
               a multiline
               comment */
            """;
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testMultiLineBlockComment", json));

        assertEquals(JSON5TokenTypeComment.JSON5_COMMENT, stream.take().type);
    }

    @Test
    void testUnterminatedBlockComment() {
        assertThrows(TokenizeException.class, () -> JSON5.tokenize("testUnterminatedBlockComment", "/* unterminated comment"));
    }

    // ===== Whitespace Token Tests =====

    @Test
    void testBasicWhitespace() {
        String json = "   \t\n  ";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testBasicWhitespace", json));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypeWhitespace.JSON5_WHITESPACE, stream.take().type);
    }

    @Test
    void testUnicodeWhitespace() {
        // Test various Unicode whitespace characters
        String json = "\u00A0\u2028\u2029\uFEFF";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testUnicodeWhitespace", json));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypeWhitespace.JSON5_WHITESPACE, stream.take().type);
    }

    @Test
    void testVerticalTab() {
        String json = "\u000B";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testVerticalTab", json));

        assertEquals(1, stream.size());
        assertEquals(JSON5TokenTypeWhitespace.JSON5_WHITESPACE, stream.take().type);
    }

    // ===== String Escape Edge Cases =====

    @Test
    void testStringWithVerticalTab() {
        String json = "\"\\v\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testStringWithVerticalTab", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("\u000B", str.value());
    }

    @Test
    void testStringWithNullCharacter() {
        String json = "\"\\0\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testStringWithNullCharacter", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("\u0000", str.value());
    }

    @Test
    void testStringWithLineContinuationCRLF() {
        String json = "\"line\\\r\ncontinuation\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testStringWithLineContinuationCRLF", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("linecontinuation", str.value());
    }

    @Test
    void testStringWithLineContinuationLF() {
        String json = "\"line\\\ncontinuation\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testStringWithLineContinuationLF", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("linecontinuation", str.value());
    }

    @Test
    void testStringWithLineContinuationCR() {
        String json = "\"line\\\rcontinuation\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testStringWithLineContinuationCR", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("linecontinuation", str.value());
    }

    @Test
    void testStringWithLineContinuationLS() {
        String json = "\"line\\\u2028continuation\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testStringWithLineContinuationLS", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("linecontinuation", str.value());
    }

    @Test
    void testStringWithLineContinuationPS() {
        String json = "\"line\\\u2029continuation\"";
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testStringWithLineContinuationPS", json));

        assertEquals(1, stream.size());
        StringValue str = (StringValue) stream.take().data;
        assertEquals("linecontinuation", str.value());
    }

    // ===== Number Format Variations =====

    @Test
    void testDecimalWithPositiveExponent() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testDecimalWithPositiveExponent", "123e+10"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("123e+10", num.repr());
        assertEquals(123e+10, num.decimalValue());
    }

    @Test
    void testDecimalWithNegativeExponent() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testDecimalWithNegativeExponent", "123e-10"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("123e-10", num.repr());
        assertEquals(123e-10, num.decimalValue());
    }

    @Test
    void testDecimalWithUppercaseExponent() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testDecimalWithUppercaseExponent", "123E10"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("123E10", num.repr());
        assertEquals(123E10, num.decimalValue());
    }

    @Test
    void testMixedCaseHexNumber() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testMixedCaseHexNumber", "0xAbCdEf"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("0xAbCdEf", num.repr());
        assertEquals(0xABCDEF, num.decimalValue());
        assertTrue(num.isInteger());
        assertEquals(0xABCDEFL, num.integerValue());
    }

    @Test
    void testUppercaseHexPrefix() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testUppercaseHexPrefix", "0XFF"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("0XFF", num.repr());
        assertEquals(0xFF, num.decimalValue());
    }

    @Test
    void testLeadingDecimalPoint() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testLeadingDecimalPoint", ".5"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals(".5", num.repr());
        assertEquals(0.5, num.decimalValue());
    }

    @Test
    void testFractionWithExponent() {
        TokenStream stream = assertDoesNotThrow(() -> JSON5.tokenize("testFractionWithExponent", "0.5e2"));

        assertEquals(1, stream.size());
        NumberValue num = (NumberValue) stream.take().data;
        assertEquals("0.5e2", num.repr());
        assertEquals(0.5e2, num.decimalValue());
    }
}
