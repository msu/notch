package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

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
