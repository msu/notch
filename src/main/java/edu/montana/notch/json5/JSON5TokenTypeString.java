package edu.montana.notch.json5;

import edu.montana.notch.chisel.*;
import edu.montana.notch.util.Text;

public class JSON5TokenTypeString implements TokenType {
    public static final JSON5TokenTypeString JSON5_STRING = new JSON5TokenTypeString();

    private JSON5TokenTypeString() {
    }

    /**
     * Represents a parsed JSON5 string with its original representation and parsed decimalValue
     */
    public record StringValue(String repr, String value) {
        public static StringValue of(String repr, String value) {
            return new StringValue(repr, value);
        }

        public char quoteChar() {
            return repr.charAt(0);
        }

        public boolean isSingleQuoted() {
            return quoteChar() == '\'';
        }

        public boolean isDoubleQuoted() {
            return quoteChar() == '"';
        }
    }

    private char lexUnicodeEscape(Tokenizer t, int digits) {
        int val = 0;
        for (int i = 0; i < digits; i++) {
            if (t.atEnd()) {
                final var diag = new Diagnostic();
                diag.highlight(t.currentCharSpan());
                diag.note("expected unicode escape hex digit");
                throw new TokenizeException(diag);
            }

            char c = t.take();
            int digit = Character.digit(c, 16);
            if (digit < 0) {
                final var diag = new Diagnostic();
                diag.highlight(t.currentCharSpan());
                diag.note("Invalid hex escape");
                throw new TokenizeException(diag);
            }
            val = (val << 4) | digit;
        }
        return (char) val;
    }

    private Character lexEscape(Tokenizer t) {
        if (t.take("\r\n") || t.take('\r', '\n', '\u2028', '\u2029')) {
            return null;
        }
        char c = t.take();
        if (c == '\'') return '\'';
        if (c == '"') return '\"';
        if (c == '\\') return '\\';
        if (c == 'b') return '\b';
        if (c == 'f') return '\f';
        if (c == 'n') return '\n';
        if (c == 'r') return '\r';
        if (c == 't') return '\t';
        if (c == 'v') return '\u000B';
        if (c == '0') return '\u0000';
        if (c == 'x') return lexUnicodeEscape(t, 2);
        if (c == 'u') return lexUnicodeEscape(t, 4);
        final var diag = new Diagnostic();
        diag.highlight(t.currentCharSpan());
        diag.note("invalid escape %s, U+%s".formatted(Text.repr(c), Integer.toHexString(c)));
        throw new TokenizeException(diag);
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        if (!t.peek('"', '\'')) return null;
        var quote = t.take();

        var content = new StringBuilder();

        while (!t.atEnd() && !t.peek(quote, '\n')) {
            final var c = t.take();

            if (c == '\\') {
                var esc = lexEscape(t);
                if (esc != null) {
                    content.append(esc);
                }
            } else if (Character.isDefined(c)) {
                content.append(c);
            } else {
                final var diag = new Diagnostic();
                final var span = new Span(t.source(), start, t.location());
                diag.highlight(span);
                diag.note("unexpected character, u" + Integer.toHexString(c));
                throw new TokenizeException(diag);
            }
        }

        if (!t.take(quote)) {
            final var diag = new Diagnostic();
            final var span = new Span(t.source(), start, t.location());
            diag.highlight(span);
            diag.note("unterminated string, expected <%s>".formatted(quote));
            throw new TokenizeException(diag);
        }

        String repr = t.source().content.subSequence(start.index, t.location().index).toString();
        String value = content.toString();

        return new TokenData(StringValue.of(repr, value));
    }

    private boolean isSourceChar(char c, char quote) {
        return Character.isDefined(c) && c != '\\' && c != quote;
    }
}
