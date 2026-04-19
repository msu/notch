package bigsky.notch.token;

import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.Tokenizer;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.TokenizeException;

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

        // a bare ':' with no terse content isn't a terse-string literal; let
        // other tokenizers (e.g. the COLON punctuator) handle it instead.
        if (lex.length() == 0) {
            t.location(start);
            return null;
        }

        return new Token(start, t.location(), this, lex.toString());
    }
}
