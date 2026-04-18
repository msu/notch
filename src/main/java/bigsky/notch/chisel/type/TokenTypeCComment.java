package bigsky.notch.chisel.type;

import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.chisel.Tokenizer;

public class TokenTypeCComment implements TokenType {
    public static final TokenTypeCComment C_COMMENT = new TokenTypeCComment();

    private TokenTypeCComment() {
    }


    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        if (!t.take("//")) return null;

//        var commentContent = new StringBuilder();
//        while (!t.atEnd() && !t.take('\n')) {
//            char c = t.take();
//            commentContent.append(c);
//        }
        String commentContent = t.seek('\n');

        return new Token(start, t.location(), this, commentContent);
    }
}
