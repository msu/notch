package edu.montana.notch.templates.token;

import edu.montana.notch.Notch;
import edu.montana.notch.chisel.*;

public class NotchTemplateTokenTypeExpression implements TokenType {
    public static final NotchTemplateTokenTypeExpression EXPRESSION = new NotchTemplateTokenTypeExpression();

    private NotchTemplateTokenTypeExpression() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        if (!t.take("${")) return null;

        var tokenizer = Notch.TOKENIZER.create(t.source(), t.location());
        var tokens = tokenizer.tokenize("}");

        t.location(tokenizer.location());
        if (!t.take('}')) {
            final var diag = new Diagnostic();
            diag.note("expected '}' after tokenized expression");
            diag.highlight(new Span(t.source(), t.location()));
            throw new TokenizeException(diag);
        }

        var data = new Data(tokens);
        return new TokenData(data);
    }

    public record Data(TokenStream notchTokens) {
    }
}
