package edu.montana.notch;

import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.Token;

import static edu.montana.notch.util.Text.repr;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenMatcher {
    static final Object UND = new Object() {
        @Override
        public String toString() {
            return "<UNDEFINED>";
        }
    };

    String expectedType;
    Object expectedData = UND;
    Location expectedStart;
    Location expectedEnd;
    Source expectedSource;

    public TokenMatcher() {
    }

    public TokenMatcher(String expectedType, Object expectedData, Location expectedStart, Location expectedEnd, Source expectedSource) {
        this.expectedType = expectedType;
        this.expectedData = expectedData;
        this.expectedStart = expectedStart;
        this.expectedEnd = expectedEnd;
        this.expectedSource = expectedSource;
    }

    public static TokenMatcher tokenMatcher(Source source, String type) {
        return new TokenMatcher().hasSource(source).hasType(type);
    }

    public static TokenMatcher tokenMatcher(Source source, String type, Object data) {
        return new TokenMatcher().hasSource(source).hasType(type).hasData(data);
    }

    public static TokenMatcher tokenMatcher(Source source, String type, int start, int end) {
        return new TokenMatcher()
                .hasSource(source)
                .hasType(type)
                .hasStart(start)
                .hasEnd(end);
    }

    public static TokenMatcher tokenMatcher(Source source, String type, Object data, int start, int end) {
        return new TokenMatcher()
                .hasSource(source)
                .hasType(type)
                .hasData(data)
                .hasStart(start)
                .hasEnd(end);
    }

    public TokenMatcher hasType(String expectedType) {
        this.expectedType = expectedType;
        return this;
    }

    public TokenMatcher hasData(Object expectedData) {
        this.expectedData = expectedData;
        return this;
    }

    public TokenMatcher hasStart(int index) {
        this.expectedStart = new Location(index, -1, -1);
        return this;
    }

    public TokenMatcher hasEnd(int index) {
        this.expectedEnd = new Location(index, -1, -1);
        return this;
    }

    public TokenMatcher hasSource(Source expectedSource) {
        this.expectedSource = expectedSource;
        return this;
    }

    public TokenMatcher hasStart(int index, int line, int column) {
        this.expectedStart = new Location(index, line, column);
        return this;
    }

    public TokenMatcher hasEnd(int index, int line, int column) {
        this.expectedEnd = new Location(index, line, column);
        return this;
    }

    public void assertMatches(Token token) {
        if (expectedType != null) {
            assertEquals(expectedType, token.type, "expected token type %s".formatted(repr(expectedType)));
        }

        if (expectedData != UND) {
            assertEquals(expectedData, token.data, "expected token data %s".formatted(repr(expectedData)));
        }

        if (expectedStart != null) {
            final var start = token.start();
            if (expectedStart.index != -1)
                assertEquals(expectedStart.index, start.index, "expected start().index = %d".formatted(expectedStart.index));
            if (expectedStart.line != -1)
                assertEquals(expectedStart.line, start.line, "expected start().line = %d".formatted(expectedStart.line));
            if (expectedStart.column != -1)
                assertEquals(expectedStart.column, start.column, "expected start().column = %d".formatted(expectedStart.column));
        }

        if (expectedEnd != null) {
            final var end = token.end();
            if (expectedEnd.index != -1)
                assertEquals(expectedEnd.index, end.index, "expected end().index = %d".formatted(expectedEnd.index));
            if (expectedEnd.line != -1)
                assertEquals(expectedEnd.line, end.line, "expected end().line = %d".formatted(expectedEnd.line));
            if (expectedEnd.column != -1)
                assertEquals(expectedEnd.column, end.column, "expected end().column = %d".formatted(expectedEnd.column));
        }

        if (expectedSource != null) {
            assertEquals(expectedSource.id, token.sourceId(), "expected sourceId() = %s".formatted(repr(expectedSource.id)));
            assertEquals(expectedSource.content, token.source().content, "expected source().content = %s".formatted(repr(expectedSource.content)));
        }
    }

}
