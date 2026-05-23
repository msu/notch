package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenData;

public class IdentTokenType implements TokenType {
    public static final IdentTokenType IDENT = new IdentTokenType();

    private IdentTokenType() {
    }

    protected boolean isStartChar(char c) {
        if (Character.isLetter(c)) return true;
        if (c == '_') return true;
        if (c == '$') return true;
        return false;
    }

    protected boolean isChar(char c) {
        if (Character.isLetterOrDigit(c)) return true;
        if (c == '_') return true;
        if (c == '$') return true;
        return false;
    }

    @Override
    public TokenData tokenize(Tokenizer t) {
        if (!isStartChar(t.peek())) return null;

        var content = new StringBuilder();
        do {
            content.append(t.take());
        } while (isChar(t.peek()));

        return new TokenData(content.toString());
    }
}
