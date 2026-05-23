package edu.montana.notch.console.token;

import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

public class PlainTokenType implements TokenType {

    public static final PlainTokenType PLAIN = new PlainTokenType();

    public PlainTokenType() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        String content = String.valueOf(t.take());
        return new TokenData(content);
    }
}
