package edu.montana.notch.console.token;

import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

public class TokenTypeInteger extends edu.montana.notch.chisel.type.TokenTypeInteger {
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