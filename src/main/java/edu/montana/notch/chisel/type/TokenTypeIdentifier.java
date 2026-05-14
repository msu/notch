package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.TokenType;

public class TokenTypeIdentifier implements TokenType {
    public static final TokenTypeIdentifier IDENT = new TokenTypeIdentifier();

    private TokenTypeIdentifier() {}

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
    public Token tokenize(Tokenizer t) {
        var start = t.location();

        if (!isStartChar(t.peek())) return null;

        var content = new StringBuilder();
        do {
            content.append(t.take());
        } while (isChar(t.peek()));

        return new Token(start, t.location(), this, content.toString());
    }

    @Override
    public String toString() {
        return "Identifier";
    }
}
