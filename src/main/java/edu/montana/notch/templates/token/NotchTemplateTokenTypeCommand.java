package edu.montana.notch.templates.token;

import edu.montana.notch.Notch;
import edu.montana.notch.chisel.*;
import edu.montana.notch.util.Exceptions;

public class NotchTemplateTokenTypeCommand implements TokenType {
    public static final NotchTemplateTokenTypeCommand COMMAND = new NotchTemplateTokenTypeCommand();

    private NotchTemplateTokenTypeCommand() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        // consume leading whitespace
        while (t.take(' ', '\t')) ;

        final var start = t.location();
        var commandName = NotchTemplateTokenTypeCommandName.COMMAND_NAME.tokenize(t);
        if (commandName == null) {
            return null;
        }
        final var commandNameToken = new Token(new Span(t.source(), start, t.location()), "commandName", commandName.value());

        var contentStart = t.location();
        t.seek('\n');
        var contentEnd = t.location();
        var commandContent = t.source().content.subSequence(0, contentEnd.index);
        var commandSource = new Source(t.source().id, commandContent);
        TokenStream contentTokens;
        try {
            contentTokens = Notch.TOKENIZER.tokenize(commandSource, contentStart);
        } catch (TokenizeException e) {
            throw Exceptions.rethrow(e);
        }

        var data = new Data(commandNameToken, contentTokens);
        return new TokenData(data);
    }

    public record Data(Token commandName, TokenStream notchTokens) {
    }
}
