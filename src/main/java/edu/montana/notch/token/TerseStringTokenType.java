package edu.montana.notch.token;

import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

public class TerseStringTokenType implements TokenType {
    public static final TerseStringTokenType TERSE_STRING = new TerseStringTokenType();

    protected TerseStringTokenType() {
    }

    protected boolean isTerseCharacter(char c) {
        return c != ' ' && c != ')' && c != ',' && c != ']' && c != '}' && c != '\0';
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        if (!t.take(':')) return null;

        var lex = new StringBuilder();
        while (isTerseCharacter(t.peek())) {
            char c = t.take();
            lex.append(c);
        }

        if (lex.isEmpty()) return null;

        return new TokenData(lex.toString());
    }
}
