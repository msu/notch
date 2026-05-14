package edu.montana.notch.templates.token;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

import static edu.montana.notch.chisel.type.TokenTypeIdentifier.IDENT;

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
