package bigsky.notch.templates.token;

import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.chisel.Tokenizer;

import static bigsky.notch.chisel.type.TokenTypeIdentifier.IDENT;

public class NotchTemplateTokenTypeCommandName implements TokenType {
    public static final NotchTemplateTokenTypeCommandName COMMAND_NAME = new NotchTemplateTokenTypeCommandName();

    private NotchTemplateTokenTypeCommandName() {}

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        if (!t.take('#')) return null;

        var name = IDENT.tokenize(t);
        if (name == null) {
            t.location(start);
            return null;
        }

        return new Token(start, t.location(), this, name.str());
    }
}
