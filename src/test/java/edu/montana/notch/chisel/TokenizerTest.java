package edu.montana.notch.chisel;

import edu.montana.notch.chisel.type.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static edu.montana.notch.AssertContains.assertContains;
import static edu.montana.notch.TokenMatcher.tokenMatcher;
import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {
    TestInfo testInfo;

    Source source(String content) {
        return new Source(testInfo.getDisplayName(), content);
    }

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        this.testInfo = testInfo;
    }


    @Test
    void testBasicTokenization() {
        Source source = source("hello world 123");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("ident", IdentTokenType.IDENT)
                .withTokenType("_ws", WhitespaceTokenType.WHITESPACE)
                .withTokenType("int", IntegerTokenType.INT)
                .create(source);

        Token token1 = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "ident", "hello", 0, 5).assertMatches(token1);

        Token token2 = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "_ws", " ", 5, 6).assertMatches(token2);

        Token token3 = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "ident", "world", 6, 11).assertMatches(token3);

        Token token4 = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "_ws", " ", 11, 12).assertMatches(token4);

        Token token5 = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "int", 123, 12, 15).assertMatches(token5);

        Token nullToken = assertDoesNotThrow(tokenizer::nextToken);
        assertNull(nullToken);
    }

    @Test
    void testPeekToken() {
        Source source = source("test");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("ident", IdentTokenType.IDENT)
                .create(source);

        Token peeked1 = assertDoesNotThrow(tokenizer::peekToken);
        Token peeked2 = assertDoesNotThrow(tokenizer::peekToken);
        assertSame(peeked1, peeked2);
        assertEquals(new Location(), tokenizer.location());

        Token consumed = assertDoesNotThrow(tokenizer::nextToken);
        assertSame(peeked1, consumed);
        assertEquals(new Location(4, 1, 5), tokenizer.location());

        Token nullPeek = assertDoesNotThrow(tokenizer::peekToken);
        assertNull(nullPeek);
    }

    @Test
    void testStringTokenization() {
        Source source = source("\"hello world\" 'single'");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("string", CStringTokenType.STR)
                .withTokenType("_ws", WhitespaceTokenType.WHITESPACE)
                .create(source);

        Token stringToken = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "string", "hello world", 0, 13).assertMatches(stringToken);

        Token wsToken = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "_ws", " ", 13, 14).assertMatches(wsToken);

        Token singleQuoteToken = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "string", "single", 14, 22).assertMatches(singleQuoteToken);
    }

    @Test
    void testStringEscapeSequences() {
        Source source = source("\"hello\\nworld\\t\\\"test\\\"\"");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("string", CStringTokenType.STR)
                .create(source);

        Token token = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "string", "hello\nworld\t\"test\"", 0, 24).assertMatches(token);
    }

    @Test
    void testUnterminatedString() {
        Source source = source("\"unterminated");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("string", CStringTokenType.STR)
                .create(source);

        TokenizeException exception = assertThrows(TokenizeException.class, tokenizer::nextToken);
        assertContains("unterminated", exception.diagnostic.getNotes().get(0));
    }

    @Test
    void testInvalidEscape() {
        Source source = source("\"invalid\\z\"");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("string", CStringTokenType.STR)
                .create(source);

        TokenizeException exception = assertThrows(TokenizeException.class, tokenizer::nextToken);
        assertContains("invalid", exception.diagnostic.getNotes().get(0));
    }

    @Test
    void testIntegerTokenization() {
        Source source = source("123 0xff 0b101 0o77");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("int", IntegerTokenType.INT)
                .withTokenType("_ws", WhitespaceTokenType.WHITESPACE)
                .create(source);

        Token decimal = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "int", 123, 0, 3).assertMatches(decimal);

        tokenizer.nextToken();

        Token hex = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "int", 255, 4, 8).assertMatches(hex);

        tokenizer.nextToken();

        Token binary = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "int", 5, 9, 14).assertMatches(binary);

        tokenizer.nextToken();

        Token octal = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "int", 63, 15, 19).assertMatches(octal);
    }

    @Test
    void testBooleanTokenization() {
        Source source = source("true false");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("bool", BooleanTokenType.BOOL)
                .withTokenType("_ws", WhitespaceTokenType.WHITESPACE)
                .create(source);

        Token trueToken = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "bool", true, 0, 4).assertMatches(trueToken);

        tokenizer.nextToken();

        Token falseToken = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "bool", false, 5, 10).assertMatches(falseToken);
    }

    @Test
    void testPunctuationTokenization() {
        Source source = source("( ) [ ] { }");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenTypes(LiteralTokenType.COMMON)
                .withTokenType("_ws", WhitespaceTokenType.WHITESPACE)
                .create(source);

        Token lparen = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "(", "(", 0, 1).assertMatches(lparen);

        tokenizer.nextToken();

        Token rparen = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, ")", ")", 2, 3).assertMatches(rparen);

        tokenizer.nextToken();

        Token lbracket = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "[", "[", 4, 5).assertMatches(lbracket);

        tokenizer.nextToken();

        Token rbracket = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "]", "]", 6, 7).assertMatches(rbracket);

        tokenizer.nextToken();

        Token lbrace = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "{", "{", 8, 9).assertMatches(lbrace);

        tokenizer.nextToken();

        Token rbrace = assertDoesNotThrow(tokenizer::nextToken);
        tokenMatcher(source, "}", "}", 10, 11).assertMatches(rbrace);
    }

    @Test
    void testTokenStream() {
        Source source = source("hello world");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("ident", IdentTokenType.IDENT)
                .withTokenType("_ws", WhitespaceTokenType.WHITESPACE)
                .create(source);

        TokenStream stream = assertDoesNotThrow(() -> tokenizer.tokenize());
        assertEquals(3, stream.tokens.size());
        assertEquals("hello", stream.tokens.get(0).str());
        assertEquals(" ", stream.tokens.get(1).str());
        assertEquals("world", stream.tokens.get(2).str());
    }

    @Test
    void testTokenStreamWithTerminalTypes() {
        Source source = source("start middle : end");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("ident", IdentTokenType.IDENT)
                .withTokenType("_ws", WhitespaceTokenType.WHITESPACE)
                .withTokenType(":", new LiteralTokenType(":"))
                .create(source);

        Token start = tokenizer.nextToken();
        assertEquals("start", start.str());

        TokenStream stream = assertDoesNotThrow(() -> tokenizer.tokenize(":"));
        assertEquals(3, stream.tokens.size());
        assertEquals(" ", stream.tokens.get(0).str());
        assertEquals("middle", stream.tokens.get(1).str());
        assertEquals(" ", stream.tokens.get(2).str());
    }

    @Test
    void testNoMatchingTokenType() {
        Source source = source("@#$");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("ident", IdentTokenType.IDENT)
                .create(source);

        TokenizeException exception = assertThrows(TokenizeException.class, tokenizer::tokenize);
        assertContains("unexpected character '@'", exception.diagnostic.getNotes().get(0));
    }

    @Test
    void testInfiniteLoopDetection() {
        TokenType brokenTokenType = t -> new TokenData();

        Source source = source("test");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("broken", brokenTokenType)
                .create(source);

        TokenizeException exception = assertThrows(TokenizeException.class, tokenizer::tokenize);
        assertContains("infinite loop", exception.getMessage());
        assertContains("infinite loop", exception.diagnostic.getNotes().get(0));
    }

    @Test
    void testPeekAndTakeOperations() {
        Source source = source("hello");
        Tokenizer tokenizer = new Tokenizer().create(source);

        assertEquals('h', tokenizer.peek());
        assertTrue(tokenizer.peek('g', 'h', 'i'));
        assertFalse(tokenizer.peek('x', 'y', 'z'));

        tokenizer = new Tokenizer().create(source);
        assertTrue(tokenizer.peek("hello"));
        assertFalse(tokenizer.peek("world"));

        tokenizer = new Tokenizer().create(source);
        assertEquals('h', tokenizer.take());
        assertEquals('e', tokenizer.take());
        assertTrue(tokenizer.take('l'));
        assertFalse(tokenizer.take('x'));

        assertTrue(tokenizer.take("lo"));
        assertTrue(tokenizer.atEnd());
    }

    @Test
    void testSeekOperations() {
        Source source = source("hello world end");
        Tokenizer tokenizer = new Tokenizer().create(source);

        String result1 = tokenizer.seek(' ');
        assertEquals("hello", result1);

        String result2 = tokenizer.trySeek(" ");
        assertEquals("world", result2);

        String result3 = tokenizer.trySeek(":");
        assertNull(result3);
        assertTrue(tokenizer.atEnd());

        assertTrue(tokenizer.atEnd());
    }

    @Test
    void testEmptySource() {
        Source source = source("");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("ident", IdentTokenType.IDENT)
                .create(source);

        assertTrue(tokenizer.atEnd());
        assertNull(tokenizer.nextToken());
        assertNull(tokenizer.peekToken());

        TokenStream stream = assertDoesNotThrow(() -> tokenizer.tokenize());
        assertTrue(stream.tokens.isEmpty());
    }

    @Test
    void testWhitespaceOnlySource() {
        Source source = source("   \t\n  ");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("_ws", WhitespaceTokenType.WHITESPACE)
                .create(source);

        TokenStream stream = assertDoesNotThrow(() -> tokenizer.tokenize());
        assertEquals(1, stream.tokens.size());
        assertEquals(source.content, stream.tokens.get(0).str());
    }

    @Test
    void testMultilineLocationTracking() {
        Source source = source("first\nsecond\nthird");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("ident", IdentTokenType.IDENT)
                .create(source);

        Token first = assertDoesNotThrow(tokenizer::nextToken);
        assertEquals(1, first.start().line);
        assertEquals(1, first.start().column);

        tokenizer.take();

        Token second = assertDoesNotThrow(tokenizer::nextToken);
        assertEquals(2, second.start().line);
        assertEquals(1, second.start().column);

        tokenizer.take();

        Token third = assertDoesNotThrow(tokenizer::nextToken);
        assertEquals(3, third.start().line);
        assertEquals(1, third.start().column);
    }

    @Test
    void testTokenizerToString() {
        Source source = source("hello world");
        Tokenizer tokenizer = new Tokenizer().create(source);

        String initial = tokenizer.toString();
        assertEquals("*hello world", initial);

        tokenizer.take(6);
        String afterTake = tokenizer.toString();
        assertEquals("hello *world", afterTake);
    }

    @Test
    void testUnicodeHandling() {
        Source source = source("café naïve résumé");
        Tokenizer tokenizer = new Tokenizer()
                .withTokenTypes("ident", IdentTokenType.IDENT)
                .withTokenType("_ws", WhitespaceTokenType.WHITESPACE)
                .create(source);

        Token cafe = assertDoesNotThrow(tokenizer::nextToken);
        assertEquals("café", cafe.str());

        tokenizer.nextToken();

        Token naive = assertDoesNotThrow(tokenizer::nextToken);
        assertEquals("naïve", naive.str());

        tokenizer.nextToken();

        Token resume = assertDoesNotThrow(tokenizer::nextToken);
        assertEquals("résumé", resume.str());
    }

    @Test
    void testLargeInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("token").append(i).append(" ");
        }
        Source source = source(sb.toString().trim());

        Tokenizer tokenizer = new Tokenizer()
                .withTokenType("ident", IdentTokenType.IDENT)
                .withTokenType("_ws", WhitespaceTokenType.WHITESPACE)
                .create(source);

        TokenStream stream = assertDoesNotThrow(() -> tokenizer.tokenize());
        assertEquals(1999, stream.tokens.size());

        assertEquals("token0", stream.tokens.get(0).str());
        assertEquals("token999", stream.tokens.get(1998).str());
    }
}