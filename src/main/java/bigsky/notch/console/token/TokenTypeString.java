package bigsky.notch.console.token;

import bigsky.notch.chisel.*;
import bigsky.notch.util.Text;

public class TokenTypeString extends bigsky.notch.chisel.type.TokenTypeString {
    public static final TokenTypeString STR = new TokenTypeString();

    protected TokenTypeString() {}

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        Location start = t.location();
        try {
            return super.tokenize(t);
        } catch (TokenizeException e) {
            Location end = t.location();
            String content = t.lex(start, end);
            return new Token(start, end, this, content);
        }
    }
}