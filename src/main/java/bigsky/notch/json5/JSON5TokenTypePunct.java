package bigsky.notch.json5;

import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.chisel.Tokenizer;

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
