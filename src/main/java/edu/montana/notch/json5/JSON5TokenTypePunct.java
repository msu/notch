package edu.montana.notch.json5;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

public enum JSON5TokenTypePunct implements TokenType {
    LeftBrace("{"),
    RightBrace("}"),
    LeftBracket("["),
    RightBracket("]"),
    Comma(","),
    Colon(":");


    public final String lex;

    JSON5TokenTypePunct(String lex) {
        this.lex = lex;
    }

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        if (!t.take(lex)) return null;
        return new Token(start, t.location(), this);
    }
}
