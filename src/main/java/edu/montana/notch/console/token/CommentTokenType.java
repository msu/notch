package edu.montana.notch.console.token;

import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

public class CommentTokenType implements TokenType {

    public static final CommentTokenType COMMENT = new CommentTokenType();

    protected CommentTokenType() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        if (t.take("#") || t.take("//")) {
            return new TokenData(t.seek('\n'));
        }
        if (t.take("/*")) {
            return new TokenData(t.seek("*/"));
        }
        return null;
    }
}
