package edu.montana.notch.util;

import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.json5.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JSON5 Parser
 */
class JSON5ParserTest {
    TestInfo info;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        this.info = testInfo;
    }

    <T> T parse(String query) {
        final var src = new Source(info.getDisplayName(), query);
        return (T) JSON5.parse(src);
    }

    // ===== Primitive Value Tests =====

    @Test
    void testParseString() {
        JSON5String result = parse("\"hello world\"");
        assertEquals("hello world", result.value);
    }

    @Test
    void testParseInteger() {
        JSON5Integer result = parse("123");
        assertEquals(123L, result.value);
    }

    @Test
    void testParseFloat() {
        JSON5Decimal result = parse("123.456");
        assertEquals(123.456, result.value);
    }

    @Test
    void testParseTrue() {
        JSON5Boolean result = parse("true");
        assertTrue(result.value);
    }

    @Test
    void testParseFalse() {
        JSON5Boolean result = parse("false");
        assertFalse(result.value);
    }

    @Test
    void testParseNull() {
        JSON5Null ignored = parse("null");
    }

    // ===== Array Tests =====

    @Test
    void testParseEmptyArray() {
        JSON5Array result = parse("[]");
        assertEquals(0, result.size());
    }

    @Test
    void testParseSimpleArray() {
        JSON5Array array = parse("[1, 2, 3]");
        assertEquals(3, array.size());
        assertEquals(1, array.get(0).intValue());
        assertEquals(2, array.get(1).intValue());
        assertEquals(3, array.get(2).intValue());
    }

    @Test
    void testParseMixedArray() {
        JSON5Array array = parse("[\"hello\", 123, true, null]");
        assertEquals(4, array.size());
        assertEquals("hello", array.get(0).stringValue());
        assertEquals(123L, array.get(1).longValue());
        assertTrue(array.get(2).booleanValue());
        assertInstanceOf(JSON5Null.class, array.get(3));
    }

    @Test
    void testParseNestedArray() {
        JSON5Array outer = parse("[[1, 2], [3, 4]]");
        assertEquals(2, outer.size());

        JSON5Array first = (JSON5Array) outer.get(0);
        assertEquals(2, first.size());
        assertEquals(1L, first.get(0).longValue());
        assertEquals(2L, first.get(1).longValue());

        JSON5Array second = (JSON5Array) outer.get(1);
        assertEquals(2, second.size());
        assertEquals(3L, second.get(0).longValue());
        assertEquals(4L, second.get(1).longValue());
    }

    @Test
    void testParseArrayWithTrailingComma() {
        JSON5Array array = parse("[1, 2, 3,]");
        assertEquals(3, array.size());
    }

    // ===== Object Tests =====

    @Test
    void testParseEmptyObject() {
        JSON5Object result = parse("{}");
        assertEquals(0, result.size());
    }

    @Test
    void testParseSimpleObject() {
        JSON5Object map = parse("{\"name\": \"John\", \"age\": 30}");
        assertEquals(2, map.size());
        assertEquals("John", map.get("name").stringValue());
        assertEquals(30L, map.get("age").longValue());
    }

    @Test
    void testParseObjectWithUnquotedKeys() {
        JSON5Object map = parse("{name: \"John\", age: 30}");
        assertEquals(2, map.size());
        assertEquals("John", map.get("name").stringValue());
        assertEquals(30L, map.get("age").longValue());
    }

    @Test
    void testParseObjectWithSingleQuotes() {
        JSON5Object map = parse("{'name': 'John', 'age': 30}");
        assertEquals(2, map.size());
        assertEquals("John", map.get("name").stringValue());
        assertEquals(30L, map.get("age").longValue());
    }

    @Test
    void testParseNestedObject() {
        JSON5Object outer = parse("{\"person\": {\"name\": \"John\", \"age\": 30}}");
        assertEquals(1, outer.size());

        JSON5Object personMap = (JSON5Object) outer.get("person");
        assertEquals("John", personMap.get("name").stringValue());
        assertEquals(30L, personMap.get("age").longValue());
    }

    @Test
    void testParseObjectWithTrailingComma() {
        JSON5Object map = parse("{\"a\": 1, \"b\": 2,}");
        assertEquals(2, map.size());
    }

    @Test
    void testParseObjectWithArray() {
        JSON5Object map = parse("{\"numbers\": [1, 2, 3]}");

        JSON5Array array = (JSON5Array) map.get("numbers");
        assertEquals(3, array.size());
    }

    // ===== Complex Structure Tests =====

    @Test
    void testParseComplexNestedStructure() {
        String json = """
                {
                    users: [
                        {name: "Alice", active: true},
                        {name: "Bob", active: false}
                    ],
                    count: 2
                }
                """;

        JSON5Object map = parse(json);

        assertEquals(2, map.size());
        assertEquals(2L, map.get("count").longValue());

        JSON5Array userList = (JSON5Array) map.get("users");
        assertEquals(2, userList.size());

        JSON5Object alice = (JSON5Object) userList.get(0);
        assertEquals("Alice", alice.get("name").stringValue());
        assertTrue(alice.get("active").booleanValue());

        JSON5Object bob = (JSON5Object) userList.get(1);
        assertEquals("Bob", bob.get("name").stringValue());
        assertFalse(bob.get("active").booleanValue());
    }

    @Test
    void testParseArrayOfObjects() {
        String json = "[{a: 1}, {b: 2}, {c: 3}]";

        JSON5Array array = parse(json);
        assertEquals(3, array.size());

        assertEquals(1L, ((JSON5Object) array.get(0)).get("a").longValue());
        assertEquals(2L, ((JSON5Object) array.get(1)).get("b").longValue());
        assertEquals(3L, ((JSON5Object) array.get(2)).get("c").longValue());
    }

    @Test
    void testParseWithSpecialNumbers() {
        String json = "{inf: Infinity, negInf: -Infinity, notANumber: NaN}";

        JSON5Object map = parse(json);

        assertTrue(Double.isInfinite(map.get("inf").doubleValue()));
        assertTrue(Double.isInfinite(map.get("negInf").doubleValue()));
        assertTrue(Double.isNaN(map.get("notANumber").doubleValue()));
    }

    @Test
    void testParseWithHexNumbers() {
        String json = "{hex: 0xFF, negHex: -0xABCD}";

        JSON5Object map = parse(json);

        assertEquals(255L, map.get("hex").longValue());
        assertEquals(-43981L, map.get("negHex").longValue());
    }

    // ===== Error Tests =====

    @Test
    void testParseInvalidJSON() {
        assertThrows(ParseException.class, () -> parse("{invalid}"));
    }

    @Test
    void testParseUnterminatedArray() {
        assertThrows(ParseException.class, () -> parse("[1, 2, 3"));
    }

    @Test
    void testParseUnterminatedObject() {
        assertThrows(ParseException.class, () -> parse("{\"a\": 1"));
    }

    @Test
    void testParseMissingColon() {
        assertThrows(ParseException.class, () -> parse("{\"a\" 1}"));
    }

    @Test
    void testParseEmptyInput() {
        assertThrows(ParseException.class, () -> parse(""));
    }

    @Test
    void testParseMissingCommaInObject() {
        var ex = assertThrows(ParseException.class, () -> parse("{a: 1 b: 2}"));
        assertTrue(ex.getMessage().contains("comma"), "Error message should mention comma");
    }

    @Test
    void testParseMissingCommaInArray() {
        var ex = assertThrows(ParseException.class, () -> parse("[1 2]"));
        assertContains("comma", ex.getMessage(), "Error message should mention comma: " + ex.getMessage());
    }

    @Test
    void testParseInvalidMemberName() {
        var ex = assertThrows(ParseException.class, () -> parse("{123: \"value\"}"));
        assertTrue(ex.getMessage().contains("member name"), "Error message should mention member name");
    }

    // ===== parseInteger() and parseDecimal() Tests =====

    @Test
    void testParseIntegerDirectly() {
        final var source = new Source(info.getDisplayName(), "42");
        JSON5Parser parser = new JSON5Parser(source);
        JSON5Integer result = parser.parseInteger();
        assertNotNull(result);
        assertEquals(42L, result.value);
    }

    @Test
    void testParseIntegerWithDecimalFails() {
        final var source = new Source(info.getDisplayName(), "42.5");
        JSON5Parser parser = new JSON5Parser(source);
        JSON5Integer result = parser.parseInteger();
        assertNull(result, "parseInteger should return null for decimal numbers");
    }

    @Test
    void testParseIntegerHex() {
        final var source = new Source(info.getDisplayName(), "0xFF");
        JSON5Parser parser = new JSON5Parser(source);
        JSON5Integer result = parser.parseInteger();
        assertNotNull(result);
        assertEquals(255L, result.value);
    }

    @Test
    void testParseDecimalDirectly() {
        final var source = new Source(info.getDisplayName(), "42.5");
        JSON5Parser parser = new JSON5Parser(source);
        JSON5Decimal result = parser.parseDecimal();
        assertNotNull(result);
        assertEquals(42.5, result.value);
    }

    @Test
    void testParseDecimalWithIntegerFails() {
        final var source = new Source(info.getDisplayName(), "42");
        JSON5Parser parser = new JSON5Parser(source);
        JSON5Decimal result = parser.parseDecimal();
        assertNull(result, "parseDecimal should return null for integer numbers");
    }

    @Test
    void testParseDecimalInfinity() {
        final var source = new Source(info.getDisplayName(), "Infinity");
        JSON5Parser parser = new JSON5Parser(source);
        JSON5Decimal result = parser.parseDecimal();
        assertNotNull(result);
        assertTrue(Double.isInfinite(result.value));
    }

    @Test
    void testParseDecimalNaN() {
        final var source = new Source(info.getDisplayName(), "NaN");
        JSON5Parser parser = new JSON5Parser(source);
        JSON5Decimal result = parser.parseDecimal();
        assertNotNull(result);
        assertTrue(Double.isNaN(result.value));
    }

    // ===== JSON with Comments Tests =====

    @Test
    void testParseObjectWithSingleLineComment() {
        String json = """
                {
                    // This is a comment
                    name: "John",
                    age: 30
                }
                """;
        JSON5Object result = parse(json);
        assertEquals(2, result.size());
        assertEquals("John", result.get("name").stringValue());
        assertEquals(30L, result.get("age").longValue());
    }

    @Test
    void testParseObjectWithBlockComment() {
        String json = """
                {
                    /* This is a
                       block comment */
                    name: "John",
                    age: 30
                }
                """;
        JSON5Object result = parse(json);
        assertEquals(2, result.size());
        assertEquals("John", result.get("name").stringValue());
        assertEquals(30L, result.get("age").longValue());
    }

    @Test
    void testParseObjectWithInlineComment() {
        String json = """
                {
                    name: "John", // inline comment
                    age: 30 /* another comment */
                }
                """;
        JSON5Object result = parse(json);
        assertEquals(2, result.size());
        assertEquals("John", result.get("name").stringValue());
        assertEquals(30L, result.get("age").longValue());
    }

    @Test
    void testParseArrayWithComments() {
        String json = """
                [
                    1, // first element
                    2, /* second element */
                    3  // third element
                ]
                """;
        JSON5Array result = parse(json);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).longValue());
        assertEquals(2L, result.get(1).longValue());
        assertEquals(3L, result.get(2).longValue());
    }

    @Test
    void testParseNestedStructureWithComments() {
        String json = """
                {
                    // User data
                    user: {
                        name: "Alice", // user name
                        /* age field */
                        age: 25
                    },
                    // Active status
                    active: true
                }
                """;
        JSON5Object result = parse(json);
        assertEquals(2, result.size());

        JSON5Object user = (JSON5Object) result.get("user");
        assertEquals("Alice", user.get("name").stringValue());
        assertEquals(25L, user.get("age").longValue());

        assertTrue(result.get("active").booleanValue());
    }

    @Test
    void testParseCommentBeforeValue() {
        String json = """
                // Comment before object
                {
                    name: "test"
                }
                """;
        JSON5Object result = parse(json);
        assertEquals(1, result.size());
        assertEquals("test", result.get("name").stringValue());
    }

    @Test
    void testParseCommentAfterValue() {
        String json = """
                {
                    name: "test"
                }
                // Comment after object
                """;
        JSON5Object result = parse(json);
        assertEquals(1, result.size());
        assertEquals("test", result.get("name").stringValue());
    }

    @Test
    void testParseMultipleConsecutiveComments() {
        String json = """
                {
                    // Comment 1
                    // Comment 2
                    /* Block comment */
                    name: "test"
                }
                """;
        JSON5Object result = parse(json);
        assertEquals(1, result.size());
        assertEquals("test", result.get("name").stringValue());
    }

    // ===== Real-World Examples =====

    @Test
    void testParsePackageJsonLike() {
        String json = """
                {
                    name: "my-package",
                    version: "1.0.0",
                    dependencies: {
                        express: "^4.17.1",
                        lodash: "~4.17.20"
                    },
                    scripts: {
                        start: "node index.js",
                        test: "jest"
                    }
                }
                """;

        JSON5Object pkg = parse(json);

        assertEquals("my-package", pkg.get("name").stringValue());
        assertEquals("1.0.0", pkg.get("version").stringValue());

        JSON5Object deps = (JSON5Object) pkg.get("dependencies");
        assertEquals("^4.17.1", deps.get("express").stringValue());

        JSON5Object scripts = (JSON5Object) pkg.get("scripts");
        assertEquals("node index.js", scripts.get("start").stringValue());
    }
}
