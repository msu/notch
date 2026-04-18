package bigsky.notch.chisel.type;

import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.chisel.Tokenizer;

public class TokenTypeBoundary implements TokenType {
    public static final TokenTypeBoundary SOF = new TokenTypeBoundary();
    public static final TokenTypeBoundary EOF = new TokenTypeBoundary();

    private TokenTypeBoundary() {
    }

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        return null;
    }
}
