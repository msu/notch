package edu.montana.notch.chisel;

import bigsky.notch.chisel.type.*;
import edu.montana.notch.chisel.type.*;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

    @Test
    void testBasicTokenization() {
        String source = "hello world 123";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenTypes(TokenTypeIdentifier.IDENT, TokenTypeWhitespace.WHITESPACE, TokenTypeInteger.INT)
            .create("testBasicTokenization", source);
        
        Token token1 = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals("hello", token1.str());
        assertEquals(TokenTypeIdentifier.IDENT, token1.type);
        assertEquals(0, token1.start.index);
        assertEquals(5, token1.end.index);
        
        Token token2 = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(" ", token2.str());
        assertEquals(TokenTypeWhitespace.WHITESPACE, token2.type);
        
        Token token3 = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals("world", token3.str());
        assertEquals(TokenTypeIdentifier.IDENT, token3.type);
        
        Token token4 = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(" ", token4.str());
        assertEquals(TokenTypeWhitespace.WHITESPACE, token4.type);
        
        Token token5 = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(123, token5.integer());
        assertEquals(TokenTypeInteger.INT, token5.type);
        
        Token nullToken = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertNull(nullToken);
    }

    @Test
    void testPeekToken() {
        String source = "test";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenType(TokenTypeIdentifier.IDENT)
            .create("testPeekToken", source);
        
        Token peeked1 = assertDoesNotThrow(() -> tokenizer.peekToken());
        Token peeked2 = assertDoesNotThrow(() -> tokenizer.peekToken());
        assertEquals(peeked1.str(), peeked2.str());
        assertEquals(peeked1.type, peeked2.type);
        
        Token consumed = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(peeked1.str(), consumed.str());
        assertEquals(peeked1.type, consumed.type);
        
        Token nullPeek = assertDoesNotThrow(() -> tokenizer.peekToken());
        assertNull(nullPeek);
    }

    @Test
    void testStringTokenization() {
        String source = "\"hello world\" 'single'";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenTypes(TokenTypeString.STR, TokenTypeWhitespace.WHITESPACE)
            .create("testStringTokenization", source);
        
        Token stringToken = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals("hello world", stringToken.str());
        assertEquals(TokenTypeString.STR, stringToken.type);
        
        Token wsToken = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(" ", wsToken.str());
        
        Token singleQuoteToken = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals("single", singleQuoteToken.str());
        assertEquals(TokenTypeString.STR, singleQuoteToken.type);
    }

    @Test
    void testStringEscapeSequences() {
        String source = "\"hello\\nworld\\t\\\"test\\\"\"";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenType(TokenTypeString.STR)
            .create("testStringEscapeSequences", source);
        
        Token token = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals("hello\nworld\t\"test\"", token.str());
    }

    @Test
    void testUnterminatedString() {
        String source = "\"unterminated";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenType(TokenTypeString.STR)
            .create("testUnterminatedString", source);
        
        TokenizeException exception = assertThrows(TokenizeException.class, () -> {
            tokenizer.nextToken();
        });
        assertTrue(exception.getMessage().contains("unterminated string"));
    }

    @Test
    void testInvalidEscape() {
        String source = "\"invalid\\z\"";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenType(TokenTypeString.STR)
            .create("testInvalidEscape", source);
        
        TokenizeException exception = assertThrows(TokenizeException.class, () -> {
            tokenizer.nextToken();
        });
        assertTrue(exception.getMessage().contains("invalid escape"));
    }

    @Test
    void testIntegerTokenization() {
        String source = "123 0xff 0b101 0o77";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenTypes(TokenTypeInteger.INT, TokenTypeWhitespace.WHITESPACE)
            .create("testIntegerTokenization", source);
        
        Token decimal = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(123, decimal.integer());
        
        tokenizer.nextToken();
        
        Token hex = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(255, hex.integer());
        
        tokenizer.nextToken();
        
        Token binary = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(5, binary.integer());
        
        tokenizer.nextToken();
        
        Token octal = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(63, octal.integer());
    }

    @Test
    void testBooleanTokenization() {
        String source = "true false";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenTypes(TokenTypeBoolean.BOOL, TokenTypeWhitespace.WHITESPACE)
            .create("testBooleanTokenization", source);
        
        Token trueToken = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertTrue(trueToken.bool());
        assertEquals(TokenTypeBoolean.BOOL, trueToken.type);
        
        tokenizer.nextToken();
        
        Token falseToken = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertFalse(falseToken.bool());
        assertEquals(TokenTypeBoolean.BOOL, falseToken.type);
    }

    @Test
    void testPunctuationTokenization() {
        String source = "( ) [ ] { }";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenTypes(TokenTypePunct.common()).withTokenType(TokenTypeWhitespace.WHITESPACE)
            .create("testPunctuationTokenization", source);
        
        Token lparen = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals("(", lparen.str());
        
        tokenizer.nextToken();
        
        Token rparen = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(")", rparen.str());
    }

    @Test
    void testTokenStream() {
        String source = "hello world";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenTypes(TokenTypeIdentifier.IDENT, TokenTypeWhitespace.WHITESPACE)
            .create("testTokenStream", source);
        
        TokenStream stream = assertDoesNotThrow(() -> tokenizer.tokenize());
        assertEquals(3, stream.tokens.size());
        assertEquals("hello", stream.tokens.get(0).str());
        assertEquals(" ", stream.tokens.get(1).str());
        assertEquals("world", stream.tokens.get(2).str());
    }

    // TODO dillon review this test and rewrite if necessary
//    @Test
//    void testTokenStreamWithTerminalTypes() {
//        String source = "start middle end";
//        TokenType terminatorType = TokenTypeIdentifier.IDENT;
//        Tokenizer tokenizer = new Tokenizer(source)
//            .with(TokenTypeIdentifier.IDENT, TokenTypeWhitespace.WHITESPACE);
//
//        tokenizer.nextToken();
//        tokenizer.nextToken();
//
//        TokenStream stream = assertDoesNotThrow(() -> tokenizer.tokenize(terminatorType));
//        assertEquals(1, stream.tokens.size());
//        assertEquals("middle", stream.tokens.get(0).str());
//    }

    @Test
    void testNoMatchingTokenType() {
        String source = "@#$";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenType(TokenTypeIdentifier.IDENT)
            .create("testNoMatchingTokenType", source);
        
        TokenizeException exception = assertThrows(TokenizeException.class, () -> {
            tokenizer.tokenize();
        });
        assertTrue(exception.getMessage().contains("expected token"));
    }

    @Test
    void testInfiniteLoopDetection() {
        TokenType brokenTokenType = new TokenType() {
            @Override
            public Token tokenize(Tokenizer t) {
                return new Token(t.location(), t.location(), this, "broken");
            }
        };
        
        String source = "test";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenType(brokenTokenType)
            .create("testInfiniteLoopDetection", source);
        
        TokenizeException exception = assertThrows(TokenizeException.class, () -> {
            tokenizer.tokenize();
        });
        assertTrue(exception.getMessage().contains("infinite loop"));
    }

    @Test
    void testLocationTracking() {
        String source = "line1\nline2";
        Location customLocation = new Location(0, 1, 1);
        Tokenizer tokenizer = new Tokenizer("testLocationTracking", source, customLocation)
            .withTokenType(TokenTypeIdentifier.IDENT);
        
        Token token1 = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(1, token1.start.line);
        assertEquals(1, token1.start.column);
        assertEquals(1, token1.end.line);
        assertEquals(6, token1.end.column);
        
        tokenizer.take();
        
        Token token2 = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(2, token2.start.line);
        assertEquals(1, token2.start.column);
    }

    @Test
    void testPeekAndTakeOperations() {
        String source = "hello";
        Tokenizer tokenizer = new Tokenizer().create("testPeekAndTakeOperations", source);

        assertEquals('h', tokenizer.peek());
        assertTrue(tokenizer.peek('h', 'e', 'l'));
        assertFalse(tokenizer.peek('x', 'y', 'z'));

        tokenizer = new Tokenizer().create("testPeekAndTakeOperations", source);
        assertTrue(tokenizer.peek("hello"));
        assertFalse(tokenizer.peek("world"));

        tokenizer = new Tokenizer().create("testPeekAndTakeOperations", source);
        assertEquals('h', tokenizer.take());
        assertEquals('e', tokenizer.take());
        assertTrue(tokenizer.take('l'));
        assertFalse(tokenizer.take('x'));

        assertTrue(tokenizer.take("lo"));
        assertTrue(tokenizer.atEnd());
    }

    @Test
    void testSeekOperations() {
        String source = "hello world end";
        Tokenizer tokenizer = new Tokenizer().create("testSeekOperations", source);
        
        String result1 = tokenizer.seek(' ');
        assertEquals("hello", result1);
        
        String result2 = tokenizer.seek("end");
        assertEquals("world ", result2);
        
        assertTrue(tokenizer.atEnd());
    }

    @Test
    void testComplexExpression() {
        String source = "if (x == \"hello\") { return 42; }";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenTypes(TokenTypeIdentifier.IDENT, TokenTypeWhitespace.WHITESPACE,
                    TokenTypeString.STR, TokenTypeInteger.INT).withTokenTypes(TokenTypePunct.common())
            .create("testComplexExpression", source);
        
        TokenStream stream = assertDoesNotThrow(() -> tokenizer.tokenize());
        assertTrue(stream.tokens.size() > 10);
        
        assertEquals("if", stream.tokens.get(0).str());
        assertEquals("(", stream.tokens.get(2).str());
        assertEquals("x", stream.tokens.get(3).str());
        assertEquals("==", stream.tokens.get(5).str());
        assertEquals("hello", stream.tokens.get(7).str());
        assertTrue(stream.tokens.stream().anyMatch(t -> t.type == TokenTypeInteger.INT && t.integer() == 42));
    }

    @Test
    void testEmptySource() {
        String source = "";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenType(TokenTypeIdentifier.IDENT)
            .create("testEmptySource", source);
        
        assertTrue(tokenizer.atEnd());
        assertNull(tokenizer.nextToken());
        assertNull(tokenizer.peekToken());
        
        TokenStream stream = assertDoesNotThrow(() -> tokenizer.tokenize());
        assertTrue(stream.tokens.isEmpty());
    }

    @Test
    void testWhitespaceOnlySource() {
        String source = "   \t\n  ";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenType(TokenTypeWhitespace.WHITESPACE)
            .create("testWhitespaceOnlySource", source);
        
        TokenStream stream = assertDoesNotThrow(() -> tokenizer.tokenize());
        assertEquals(1, stream.tokens.size());
        assertEquals(source, stream.tokens.get(0).str());
    }

    @Test
    void testMultilineLocationTracking() {
        String source = "first\nsecond\nthird";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenType(TokenTypeIdentifier.IDENT)
            .create("testMultilineLocationTracking", source);
        
        Token first = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(1, first.start.line);
        assertEquals(1, first.start.column);
        
        tokenizer.take();
        
        Token second = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(2, second.start.line);
        assertEquals(1, second.start.column);
        
        tokenizer.take();
        
        Token third = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals(3, third.start.line);
        assertEquals(1, third.start.column);
    }

    @Test
    void testPatternTokenization() {
        String source = "match123pattern";
        TokenTypePattern customPattern = new TokenTypePattern(Pattern.compile("match\\d+pattern"));
        Tokenizer tokenizer = new Tokenizer()
            .withTokenTypes(customPattern, TokenTypeIdentifier.IDENT)
            .create("testPatternTokenization", source);
        
        Token matched = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals("match123pattern", matched.str());
        assertEquals(customPattern, matched.type);
    }

    @Test
    void testTokenizerToString() {
        String source = "hello world";
        Tokenizer tokenizer = new Tokenizer().create("testTokenizerToString", source);
        
        String initial = tokenizer.toString();
        assertEquals("*hello world", initial);
        
        tokenizer.take(6);
        String afterTake = tokenizer.toString();
        assertEquals("hello *world", afterTake);
    }

    @Test
    void testUnicodeHandling() {
        String source = "café naïve résumé";
        Tokenizer tokenizer = new Tokenizer()
            .withTokenTypes(TokenTypeIdentifier.IDENT, TokenTypeWhitespace.WHITESPACE)
            .create("testUnicodeHandling", source);
        
        Token cafe = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals("café", cafe.str());
        
        tokenizer.nextToken();
        
        Token naive = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals("naïve", naive.str());
        
        tokenizer.nextToken();
        
        Token resume = assertDoesNotThrow(() -> tokenizer.nextToken());
        assertEquals("résumé", resume.str());
    }

    @Test
    void testLargeInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("token").append(i).append(" ");
        }
        String source = sb.toString().trim();

        Tokenizer tokenizer = new Tokenizer()
            .withTokenTypes(TokenTypeIdentifier.IDENT, TokenTypeWhitespace.WHITESPACE)
            .create("testLargeInput", source);
        
        TokenStream stream = assertDoesNotThrow(() -> tokenizer.tokenize());
        assertEquals(1999, stream.tokens.size());
        
        assertEquals("token0", stream.tokens.get(0).str());
        assertEquals("token999", stream.tokens.get(1998).str());
    }
}