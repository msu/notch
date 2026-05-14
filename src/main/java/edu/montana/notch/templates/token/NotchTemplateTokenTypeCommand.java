package edu.montana.notch.templates.token;

import edu.montana.notch.Notch;
import bigsky.notch.chisel.*;
import edu.montana.notch.chisel.*;

public class NotchTemplateTokenTypeCommand implements TokenType {
    public static final NotchTemplateTokenTypeCommand COMMAND = new NotchTemplateTokenTypeCommand();

    private NotchTemplateTokenTypeCommand() {
    }

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();

        // consume leading whitespace
        while (t.take(' ', '\t')) ;

        var commandName = NotchTemplateTokenTypeCommandName.COMMAND_NAME.tokenize(t);
        if (commandName == null) {
            return null;
        }

        var contentStart = t.location();
        t.seek('\n');
        var contentEnd = t.location();
        var contentSrc = t.source().subSequence(0, contentEnd.index);
        TokenStream contentTokens;
        try {
            contentTokens = Notch.TOKENIZER.tokenize(t.fileId(), contentSrc, contentStart);
        } catch (TokenizeException e) {
            throw new TokenizeException(new Span(contentStart, contentEnd), "failed to parse command tokens", e);
        }

        var data = new Data(commandName, contentTokens);
        return new Token(start, t.location(), this, data);
    }

    public record Data(Token commandName, TokenStream notchTokens) {
    }
}
