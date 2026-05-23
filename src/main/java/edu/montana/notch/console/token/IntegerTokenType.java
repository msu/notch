package edu.montana.notch.console.token;

import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

public class IntegerTokenType extends edu.montana.notch.chisel.type.IntegerTokenType {
    public static final IntegerTokenType INT = new IntegerTokenType();

    protected IntegerTokenType() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        Location start = t.location();
        try {
            return super.tokenize(t);
        } catch (TokenizeException | NumberFormatException e) {
            Location end = t.location();
            String content = t.lex(start, end);
            return new TokenData(content);
        }
    }
}