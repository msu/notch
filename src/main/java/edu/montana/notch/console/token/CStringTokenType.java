package edu.montana.notch.console.token;

import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

public class CStringTokenType extends edu.montana.notch.chisel.type.CStringTokenType {
    public static final CStringTokenType STR = new CStringTokenType();

    protected CStringTokenType() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        Location start = t.location();
        try {
            return super.tokenize(t);
        } catch (TokenizeException e) {
            Location end = t.location();
            String content = t.lex(start, end);
            return new TokenData(content);
        }
    }
}