package bigsky.notch.chisel.type;

import bigsky.notch.chisel.*;

import static bigsky.notch.chisel.type.CIdentifierTokenType.IDENT;

public class BooleanTokenType implements TokenType {
    public static final BooleanTokenType BOOL = new BooleanTokenType();

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        var ident = IDENT.tokenize(t);
        if (ident == null) return null;

        final String lex = ident.str();
        if (lex.equals("true")) {
            return new TokenData(true);
        } else if (lex.equals("false")) {
            return new TokenData(false);
        } else {
            return null;
        }
    }
}
