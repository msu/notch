package edu.montana.notch.console.token;

import bigsky.notch.chisel.*;
import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

public class TokenTypeString extends edu.montana.notch.chisel.type.TokenTypeString {
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