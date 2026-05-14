package edu.montana.notch.json5;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

import java.util.Set;

public class JSON5TokenTypeWhitespace implements TokenType {
    public static final JSON5TokenTypeWhitespace JSON5_WHITESPACE = new JSON5TokenTypeWhitespace();
    private JSON5TokenTypeWhitespace() {}

    public static final Set<Character> WHITESPACE_CHARS = Set.of(
            '\u0009',
            '\n',
            '\u000B',
            '\u0020',
            '\u00A0',
            '\u2028',
            '\u2029',
            '\uFEFF'
    );

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        while (!t.atEnd()) {
            var c = t.peek();
            if (WHITESPACE_CHARS.contains(c)) {
                t.take();
                continue;
            }

            if (Character.getType(c) == Character.SPACE_SEPARATOR) {
                t.take();
                continue;
            }

            break;
        }

        if (start.equals(t.location())) {
            return null;
        }
        return new Token(start, t.location(), this, null);
    }
}
