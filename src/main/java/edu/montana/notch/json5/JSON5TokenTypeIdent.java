package edu.montana.notch.json5;

import edu.montana.notch.chisel.*;
import edu.montana.notch.util.Text;

import java.util.Set;

public final class JSON5TokenTypeIdent implements TokenType {
    public static final JSON5TokenTypeIdent JSON5_IDENT = new JSON5TokenTypeIdent();

    public static final Set<String> KEYWORDS = Set.of("true", "false", "null");
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
                final var diag = new Diagnostic();
                diag.highlight(new Span(t.source(), start, t.location()));
                diag.note("expected hex digit in escape");
                throw new TokenizeException(diag);
            }
            codePoint = (codePoint << 4) + Text.hexValue(ch);
            t.take();
        }

        // Validate the resulting code point
        if (!Character.isLetter(codePoint)) {
            final var diag = new Diagnostic();
            diag.highlight(new Span(t.source(), start, t.location()));
            diag.note("expected hex digit in escape");
            throw new TokenizeException(diag);
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
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        var lexB = new StringBuilder();

        if (!lexIdentStart(t, lexB)) {
            return null;
        }

        do ; while (lexIdentPart(t, lexB));
        var end = t.location();

        var lex = lexB.toString();
        if (KEYWORDS.contains(lex)) {
            return new TokenData(lex, lex);
        }

        if (RESERVED.contains(lex)) {
            final var diag = new Diagnostic();
            diag.highlight(new Span(t.source(), start, end));
            diag.note("reserved keyword: " + Text.repr(lex));
            throw new TokenizeException(diag);
        }

        return new TokenData(lex);
    }
}
