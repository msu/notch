package bigsky.notch.json5;

import bigsky.notch.util.Text;
import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.chisel.Tokenizer;

import java.util.Set;

public final class JSON5TokenTypeIdent implements TokenType {
    public enum Keyword implements TokenType {
        JSON_TRUE("true"),
        JSON_FALSE("false"),
        JSON_NULL("null");

        public final String lex;

        Keyword(String lex) {
            this.lex = lex;
        }

        @Override
        public Token tokenize(Tokenizer t) throws TokenizeException {
            var ident = JSON5_IDENT.tokenize(t);
            if (ident == null || ident.type != this) return null;
            return ident;
        }
    }

    public static final JSON5TokenTypeIdent JSON5_IDENT = new JSON5TokenTypeIdent();

    public static final Set<String> RESERVED = Set.of("break", "do", "instanceof", "typeof", "case", "else", "new",
            "var", "catch", "finally", "return", "void", "continue", "for", "switch", "while", "debugger", "function",
            "this", "with", "default", "if", "throw", "delete", "in", "try", "class", "enum", "extends", "super",
            "const", "export", "import", "null", "true", "false");

    private JSON5TokenTypeIdent() {
    }

    private boolean lexIdentStart(Tokenizer t, StringBuilder lexB) {
        final var c = t.peek();
        if (c == '$' || c == '_' || Character.isLetter(c)) {
            t.take();
            lexB.append(c);
            return true;
        }

        if (c == '\\') {
            return lexUnicodeEscape(t, lexB);
        }

        return false;
    }

    private boolean lexUnicodeEscape(Tokenizer t, StringBuilder lexB) {
        var start = t.location();
        if (!t.take("\\u")) return false;

        int codePoint = 0;
        for (int i = 0; i < 4; i++) {
            char ch = t.peek();
            if (!Text.isHexDigit(ch)) {
                throw new TokenizeException(start, t.location(), "expected hex digit in escape");
            }
            codePoint = (codePoint << 4) + Text.hexValue(ch);
            t.take();
        }

        // Validate the resulting code point
        if (!Character.isLetter(codePoint)) {
            throw new TokenizeException(start, t.location(), "expected hex digit in escape");
        }

        lexB.append((char) codePoint);
        return true;
    }

    private static final char ZWNJ = '\u200C';
    private static final char ZWJ = '\u200D';

    private boolean lexIdentPart(Tokenizer t, StringBuilder lexB) {
        var c = t.peek();
        if (c == '_' || c == '$' || Character.isLetter(c)) {
            t.take();
            lexB.append(c);
            return true;
        }

        if (c == '\\') {
            return lexUnicodeEscape(t, lexB);
        }

        int type = Character.getType(c);
        return switch (type) {
            case Character.NON_SPACING_MARK,
                 Character.COMBINING_SPACING_MARK,
                 Character.DECIMAL_DIGIT_NUMBER,
                 Character.CONNECTOR_PUNCTUATION,
                 ZWNJ,
                 ZWJ -> {
                t.take();
                lexB.append(c);
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        var lexB = new StringBuilder();

        if (!lexIdentStart(t, lexB)) {
            return null;
        }

        do ; while (lexIdentPart(t, lexB));
        var end = t.location();

        var lex = lexB.toString();
        for (var tt : Keyword.values()) {
            if (lex.equals(tt.lex)) {
                return new Token(start, end, tt);
            }
        }

        for (var str : RESERVED) {
            if (lex.equals(str)) {
                throw new TokenizeException(start, end, "reserved keyword: " + Text.repr(str));
            }
        }

        return new Token(start, end, this, lex);
    }
}
