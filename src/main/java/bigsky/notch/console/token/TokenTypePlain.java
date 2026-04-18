package bigsky.notch.console.token;

import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.chisel.Tokenizer;

public class TokenTypePlain implements TokenType {

    public static final TokenTypePlain PLAIN = new TokenTypePlain();

    public TokenTypePlain() {}

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        String content = String.valueOf(t.take());
        return new Token(start, t.location(), this, content);
    }
}
