package edu.montana.notch.json5;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

public class JSON5TokenTypeComment implements TokenType {
    public static final JSON5TokenTypeComment JSON5_COMMENT = new JSON5TokenTypeComment();

    private JSON5TokenTypeComment() {}

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        final var start = t.location();

        if (t.take("//")) {
            t.seek('\n');
            return new Token(start, t.location(), this, null);
        }

        if (t.take("/*")) {
            if (null == t.trySeek("*/")) {
                throw new TokenizeException(start, t.location(), "expected '*/' in tokens");
            }
            return new Token(start, t.location(), this, null);
        }

        return null;
    }
}
