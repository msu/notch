package bigsky.notch.console.token;

import bigsky.notch.chisel.Location;
import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.chisel.Tokenizer;

public class TokenTypeInteger extends bigsky.notch.chisel.type.TokenTypeInteger {
    public static final TokenTypeInteger NUM = new TokenTypeInteger();

    protected TokenTypeInteger() {}

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        Location start = t.location();
        try {
            return super.tokenize(t);
        } catch (TokenizeException | NumberFormatException e) {
            Location end = t.location();
            String content = t.lex(start, end);
            return new Token(start, end, this, content);
        }
    }
}