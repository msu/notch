package edu.montana.notch.util;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

class TextTest {

    @Test
    void testReprString() {
        String simple = "hello";
        assertEquals("\"hello\"", Text.repr(simple));

        String withQuotes = "hello \"world\"";
        assertEquals("\"hello \\\"world\\\"\"", Text.repr(withQuotes));

        String withSingleQuotes = "hello 'world'";
        assertEquals("\"hello \\'world\\'\"", Text.repr(withSingleQuotes));

        String withBackslash = "path\\to\\file";
        assertEquals("\"path\\\\to\\\\file\"", Text.repr(withBackslash));

        String withEscapes = "hello\nworld\ttest";
        assertEquals("\"hello\\nworld\\ttest\"", Text.repr(withEscapes));

        String empty = "";
        assertEquals("\"\"", Text.repr(empty));
    }

    @Test
    void testReprChar() {
        char simple = 'a';
        assertEquals("'a'", Text.repr(simple));

        char quote = '"';
        assertEquals("'\\\"'", Text.repr(quote));

        char singleQuote = '\'';
        assertEquals("'\\''", Text.repr(singleQuote));

        char backslash = '\\';
        assertEquals("'\\\\'", Text.repr(backslash));

        char newline = '\n';
        assertEquals("'\\n'", Text.repr(newline));

        char tab = '\t';
        assertEquals("'\\t'", Text.repr(tab));

        char backspace = '\b';
        assertEquals("'\\b'", Text.repr(backspace));

        char formFeed = '\f';
        assertEquals("'\\f'", Text.repr(formFeed));

        char carriageReturn = '\r';
        assertEquals("'\\r'", Text.repr(carriageReturn));
    }

    @Test
    void testReprSpecialCharacters() {
        char controlChar = (char) 7; // bell character
        String result = Text.repr(controlChar);
        assertContains("\\u{7}", result);

        char unicodeChar = (char) 200;
        String unicodeResult = Text.repr(unicodeChar);
        assertContains("\\u{c8}", unicodeResult);

        String stringWithSpecialChars = "test\u0007\u00C8end";
        String stringResult = Text.repr(stringWithSpecialChars);
        assertContains("\\u{7}", stringResult);
        assertContains("\\u{c8}", stringResult);
    }

    @Test
    void testDecapitalize() {
        String capitalized = "Hello";
        assertEquals("hello", Text.decapitalize(capitalized));

        String alreadyLower = "hello";
        assertEquals("hello", Text.decapitalize(alreadyLower));

        String singleChar = "A";
        assertEquals("a", Text.decapitalize(singleChar));

        String empty = "";
        assertEquals("", Text.decapitalize(empty));

        String nullString = null;
        assertNull(Text.decapitalize(nullString));

        String withNumbers = "Test123";
        assertEquals("test123", Text.decapitalize(withNumbers));
    }

    @Test
    void testCapitalize() {
        String lowercase = "hello";
        assertEquals("Hello", Text.capitalize(lowercase));

        String alreadyCapitalized = "Hello";
        assertEquals("Hello", Text.capitalize(alreadyCapitalized));

        String singleChar = "a";
        assertEquals("A", Text.capitalize(singleChar));

        String empty = "";
        assertEquals("", Text.capitalize(empty));

        String nullString = null;
        assertNull(Text.capitalize(nullString));

        String withNumbers = "test123";
        assertEquals("Test123", Text.capitalize(withNumbers));
    }

    @Test
    void testIndent() {
        String singleLine = "hello world";
        assertEquals("  hello world", Text.indent(2, singleLine));

        String multiLine = "line1\nline2\nline3";
        String expected = "    line1\n    line2\n    line3";
        assertEquals(expected, Text.indent(4, multiLine));

        String empty = "";
        assertEquals("  ", Text.indent(2, empty));

        String withEmptyLines = "line1\n\nline3";
        String expectedWithEmpty = "  line1\n  \n  line3";
        assertEquals(expectedWithEmpty, Text.indent(2, withEmptyLines));

        String zeroIndent = "test";
        assertEquals("test", Text.indent(0, zeroIndent));
    }

    @Test
    void testSnakeCase() {
        String camelCase = "camelCaseString";
        assertEquals("camel_case_string", Text.snakeCase(camelCase));

        String pascalCase = "PascalCaseString";
        assertEquals("pascal_case_string", Text.snakeCase(pascalCase));

        String alreadySnake = "snake_case_string";
        assertEquals("snake_case_string", Text.snakeCase(alreadySnake));

        String singleWord = "word";
        assertEquals("word", Text.snakeCase(singleWord));

        String empty = "";
        assertEquals("", Text.snakeCase(empty));

        String withNumbers = "myVar123Name";
        assertEquals("my_var123_name", Text.snakeCase(withNumbers));

        String consecutiveCapitals = "XMLHttpRequest";
        assertEquals("x_m_l_http_request", Text.snakeCase(consecutiveCapitals));
    }

    @Test
    void testCamelCase() {
        String snakeCase = "snake_case_string";
        assertEquals("snakeCaseString", Text.camelCase(snakeCase));

        String withUnderscores = "my_variable_name";
        assertEquals("myVariableName", Text.camelCase(withUnderscores));

        String singleWord = "word";
        assertEquals("word", Text.camelCase(singleWord));

        String empty = "";
        assertEquals("", Text.camelCase(empty));

        String withNumbers = "my_var_123_name";
        assertEquals("myVar123Name", Text.camelCase(withNumbers));

        String startingUnderscore = "_leading_underscore";
        assertEquals("_leadingUnderscore", Text.camelCase(startingUnderscore));

        String trailingUnderscore = "trailing_underscore_";
        assertEquals("trailingUnderscore_", Text.camelCase(trailingUnderscore));

        String consecutiveUnderscores = "double__underscore";
        assertEquals("double_Underscore", Text.camelCase(consecutiveUnderscores));
    }

    @Test
    void testDeSnakeCase() {
        String snakeCase = "snake_case_string";
        assertEquals("snakeCaseString", Text.deSnakeCase(snakeCase));

        String withUnderscores = "my_variable_name";
        assertEquals("myVariableName", Text.deSnakeCase(withUnderscores));

        String singleWord = "word";
        assertEquals("word", Text.deSnakeCase(singleWord));

        String empty = "";
        assertEquals("", Text.deSnakeCase(empty));

        String withNumbers = "my_var_123_name";
        assertEquals("myVar123Name", Text.deSnakeCase(withNumbers));

        String startingUnderscore = "_leading_underscore";
        assertEquals("LeadingUnderscore", Text.deSnakeCase(startingUnderscore));

        String trailingUnderscore = "trailing_underscore_";
        assertEquals("trailingUnderscore_", Text.deSnakeCase(trailingUnderscore));
    }

    @Test
    void testPluralize() {
        String regularNoun = "cat";
        assertEquals("cats", Text.pluralize(regularNoun));

        String dog = "dog";
        assertEquals("dogs", Text.pluralize(dog));

        String wordEndingInCh = "church";
        assertEquals("churches", Text.pluralize(wordEndingInCh));

        String wordEndingInSh = "wish";
        assertEquals("wishes", Text.pluralize(wordEndingInSh));

        String wordEndingInSs = "class";
        assertEquals("classes", Text.pluralize(wordEndingInSs));

        String wordEndingInY = "city";
        assertEquals("cities", Text.pluralize(wordEndingInY));

        String wordEndingInVowelY = "boy";
        assertEquals("boys", Text.pluralize(wordEndingInVowelY));

        String wordEndingInF = "leaf";
        assertEquals("leaves", Text.pluralize(wordEndingInF));

        String wordEndingInFe = "knife";
        assertEquals("knives", Text.pluralize(wordEndingInFe));

        String shelf = "shelf";
        assertEquals("shelves", Text.pluralize(shelf));

        String wolf = "wolf";
        assertEquals("wolves", Text.pluralize(wolf));

        String life = "life";
        assertEquals("lives", Text.pluralize(life));

        String wife = "wife";
        assertEquals("wives", Text.pluralize(wife));

        String scarf = "scarf";
        assertEquals("scarves", Text.pluralize(scarf));
    }

    @Test
    void testIsLowerCase() {
        String lowercase = "helloworld";
        assertTrue(Text.isLowerCase(lowercase));

        String withUppercase = "HelloWorld";
        assertFalse(Text.isLowerCase(withUppercase));

        String mixed = "heLLo";
        assertFalse(Text.isLowerCase(mixed));

        String withNumbers = "hello123";
        assertFalse(Text.isLowerCase(withNumbers));

        String withSymbols = "hello!@#";
        assertFalse(Text.isLowerCase(withSymbols));

        String onlyLowerLetters = "abcdefg";
        assertTrue(Text.isLowerCase(onlyLowerLetters));

        String empty = "";
        assertTrue(Text.isLowerCase(empty));

        String singleLower = "a";
        assertTrue(Text.isLowerCase(singleLower));

        String singleUpper = "A";
        assertFalse(Text.isLowerCase(singleUpper));
    }

    @Test
    void testCamelCaseAndSnakeCaseRoundTrip() {
        String original = "myVariableName";
        String snaked = Text.snakeCase(original);
        String backToCamel = Text.deSnakeCase(snaked);
        assertEquals("myVariableName", backToCamel);

        String originalSnake = "my_variable_name";
        String camelized = Text.deSnakeCase(originalSnake);
        String backToSnake = Text.snakeCase(camelized);
        assertEquals("my_variable_name", backToSnake);
    }

    @Test
    void testCapitalizeDecapitalizeRoundTrip() {
        String original = "TestString";
        String decapitalized = Text.decapitalize(original);
        String backToCapitalized = Text.capitalize(decapitalized);
        assertEquals(original, backToCapitalized);

        String originalLower = "testString";
        String capitalized = Text.capitalize(originalLower);
        String backToLower = Text.decapitalize(capitalized);
        assertEquals(originalLower, backToLower);
    }

    @Test
    void testEdgeCasesAndBoundaryConditions() {
        String onlyUnderscore = "_";
        assertEquals("_", Text.camelCase(onlyUnderscore));
        assertEquals("_", Text.deSnakeCase(onlyUnderscore));

        String multipleUnderscores = "___";
        assertEquals("___", Text.camelCase(multipleUnderscores));


        String endingUnderscore = "test_";
        assertEquals("test_", Text.camelCase(endingUnderscore));

        String onlyUppercase = "TEST";
        assertEquals("t_e_s_t", Text.snakeCase(onlyUppercase));

        String mixedWithSpaces = "Hello World";
        assertEquals("hello World", Text.decapitalize(mixedWithSpaces));
    }
}