package edu.montana.notch.templates.token;

import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

import static edu.montana.notch.chisel.type.IdentTokenType.IDENT;

public class NotchTemplateTokenTypeCommandName implements TokenType {
    public static final NotchTemplateTokenTypeCommandName COMMAND_NAME = new NotchTemplateTokenTypeCommandName();

    private NotchTemplateTokenTypeCommandName() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        if (!t.take('#')) return null;

        var name = IDENT.tokenize(t);
        if (name == null) {
            t.location(start);
            return null;
        }

        return new TokenData(name.str());
    }
}
