package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;

public class TokenTypeWhitespace implements TokenType {
    public static final TokenTypeWhitespace WHITESPACE = new TokenTypeWhitespace();

    protected TokenTypeWhitespace() {}

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var lex = new StringBuilder();
        var start = t.location();
        while (!t.atEnd()) {
            if (!Character.isWhitespace(t.peek())) {
                break;
            }
            char c = t.take();
            lex.append(c);
        }

        if (start.equals(t.location())) {
            return null;
        }

        return new Token(start, t.location(), this, lex.toString());
    }
}
