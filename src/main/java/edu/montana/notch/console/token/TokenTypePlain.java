package edu.montana.notch.console.token;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

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
