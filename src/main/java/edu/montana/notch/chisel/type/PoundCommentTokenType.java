package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

public class PoundCommentTokenType implements TokenType {
    public static final PoundCommentTokenType POUND_COMMENT = new PoundCommentTokenType();

    private PoundCommentTokenType() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        if (!t.take("#")) return null;
        final var content = t.seek('\n');
        return new TokenData(content);
    }
}
