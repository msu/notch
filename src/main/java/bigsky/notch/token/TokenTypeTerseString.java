package bigsky.notch.token;

import bigsky.utils.chisel.Token;
import bigsky.utils.chisel.Tokenizer;
import bigsky.utils.chisel.TokenType;
import bigsky.utils.chisel.TokenizeException;

public class TokenTypeTerseString implements TokenType {
    public static final TokenTypeTerseString TERSE_STRING = new TokenTypeTerseString();

    protected TokenTypeTerseString() {}

    protected boolean isTerseCharacter(char c) {
        return c != ' ' && c != ')' && c != ',' && c != ']' && c != '}' && c != '\0';
    }

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();

        if (!t.take(':')) return null;

        var lex = new StringBuilder();
        while (isTerseCharacter(t.peek())) {
            char c = t.take();
            lex.append(c);
        }

        return new Token(start, t.location(), this, lex.toString());
    }
}
