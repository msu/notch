package bigsky.notch.chisel.type;

import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.Tokenizer;
import bigsky.notch.chisel.TokenType;

public class TokenTypeInteger implements TokenType {
    public static final TokenTypeInteger INT = new TokenTypeInteger();

    protected TokenTypeInteger() {}

    public static boolean isDigit(char c, int radix) {
        if (radix == 10) return c >= '0' && c <= '9';
        if (radix == 8) return c >= '0' && c <= '7';
        if (radix == 2) return c == '0' || c == '1';
        if (radix == 16) return isDigit(c, 10) || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
        throw new UnsupportedOperationException("unsupported radix " + radix);
    }

    @Override
    public Token tokenize(Tokenizer t) {
        if (!Character.isDigit(t.peek())) return null;
        var start = t.location();
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

        int value = Integer.parseInt(content.toString(), base);
        return new Token(start, t.location(), this, value);
    }
}
