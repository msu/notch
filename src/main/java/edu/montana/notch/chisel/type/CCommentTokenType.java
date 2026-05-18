package bigsky.notch.chisel.type;

import bigsky.notch.chisel.*;

public class CCommentTokenType implements TokenType {
    public static final CCommentTokenType C_COMMENT = new CCommentTokenType();

    private CCommentTokenType() {
    }


    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        if (!t.take("//")) return null;
        String commentContent = t.seek('\n');
        return new TokenData(commentContent);
    }
}
