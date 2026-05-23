package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

public class WhitespaceTokenType implements TokenType {
    public static final WhitespaceTokenType WHITESPACE = new WhitespaceTokenType();

    protected WhitespaceTokenType() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        var lex = new StringBuilder();
        while (!t.atEnd()) {
            if (!Character.isWhitespace(t.peek())) {
                break;
            }
            char c = t.take();
            lex.append(c);
        }
        if (lex.isEmpty()) return null;

        return new TokenData(lex.toString());
    }
}
