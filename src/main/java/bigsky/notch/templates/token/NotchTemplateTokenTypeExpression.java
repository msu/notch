package bigsky.notch.templates.token;

import bigsky.notch.Notch;
import bigsky.notch.chisel.*;
import bigsky.notch.chisel.type.TokenTypePunct;

public class NotchTemplateTokenTypeExpression implements TokenType {
    public static final NotchTemplateTokenTypeExpression EXPRESSION = new NotchTemplateTokenTypeExpression();

    private NotchTemplateTokenTypeExpression() {}

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        if (!t.take("${")) return null;

        var tokenizer = Notch.TOKENIZER.create(t.fileId(), t.source(), t.location());
        var tokens = tokenizer.tokenize(TokenTypePunct.RBRACE);

        t.location(tokenizer.location());
        if (!t.take('}')) {
            throw new TokenizeException(t.location(), "expected '}' after tokenized expression");
        }

        var data = new Data(tokens);
        return new Token(start, t.location(), this, data);
    }

    public record Data(TokenStream notchTokens) {}
}
