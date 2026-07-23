package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.Tokenizer;

public class IntegerTokenType implements TokenType {
    public static final IntegerTokenType INT = new IntegerTokenType();

    protected IntegerTokenType() {
    }

    public static boolean isDigit(char c, int radix) {
        if (radix == 10) return c >= '0' && c <= '9';
        if (radix == 8) return c >= '0' && c <= '7';
        if (radix == 2) return c == '0' || c == '1';
        if (radix == 16) return isDigit(c, 10) || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
        throw new UnsupportedOperationException("unsupported radix " + radix);
    }

    @Override
    public TokenData tokenize(Tokenizer t) {
        if (!Character.isDigit(t.peek())) return null;
        Location start = t.location();
        char c = t.take();

        int base = 10;
        var content = new StringBuilder();
        if (c == '0') {
            if (t.take('x')) {
                base = 16;
            } else if (t.take('b')) {
                base = 2;
            } else if (t.take('o')) {
                base = 8;
            } else {
                content.append('0');
            }
        } else {
            content.append(c);
        }

        while (isDigit(t.peek(), base)) {
            c = t.take();
            content.append(c);
        }

        try {
            int value = Integer.parseInt(content.toString(), base);
            return new TokenData(value);
        } catch (NumberFormatException e) {
            var diag = new Diagnostic()
                    .note("number too large: " + content + " exceeds the maximum integer value")
                    .highlight(new Span(t.source(), start, t.location()));
            throw new TokenizeException(diag);
        }
    }
}
