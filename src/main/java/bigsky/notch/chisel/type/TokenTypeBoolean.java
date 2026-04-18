package bigsky.notch.chisel.type;

import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.chisel.Tokenizer;

import static bigsky.notch.chisel.type.TokenTypeIdentifier.IDENT;

public class TokenTypeBoolean implements TokenType {
    public static final TokenTypeBoolean BOOL = new TokenTypeBoolean();

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var ident = IDENT.tokenize(t);
        if (ident == null) return null;

        boolean value;
        if (ident.str().equals("true")) value = true;
        else if (ident.str().equals("false")) value = false;
        else return null;

        return new Token(ident.start, ident.end, this, value);
    }
}
