package edu.montana.notch.token;

import edu.montana.notch.chisel.*;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.Text;

public class NotchStringTokenType implements TokenType {
    public static final NotchStringTokenType STR = new NotchStringTokenType();

    protected NotchStringTokenType() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();

        if (!t.peek('"', '\'', '`')) return null;
        char quote = t.take();

        BetterList<Object> pieces = new BetterList<>();
        var content = new StringBuilder();
        while (!t.atEnd()) {
            char c = t.peek();
            if (c == quote) {
                break;
            }

            t.take();
            if (c == '}' && !t.take('}')) {
                final var diag = new Diagnostic();
                diag.note("this brace does not match a corresponding '{' for value interpolation");
                diag.highlight(t.currentCharSpan());
                throw new TokenizeException(diag);
            } else if (c == '\\') {
                tokenizeEscape(quote, start, t, content);
            } else if (c == '{' && !t.take('{')) {
                var expression = FStringTokenData.tokenizeFExpression(t);
                if (!content.isEmpty()) {
                    pieces.add(content.toString());
                    content.setLength(0);
                }
                pieces.add(expression);
            } else {
                content.append(c);
            }
        }

        if (!t.take(quote)) {
            final var diag = new Diagnostic();
            diag.highlight(new Span(t.source(), t.location(), t.location()));
            diag.note("unterminated string, expected the quote character " + quote);
            throw new TokenizeException(diag);
        }

        if (!content.isEmpty()) {
            pieces.add(content.toString());
        }
        return new TokenData(new FStringTokenData(pieces));
    }

    private void tokenizeEscape(char quote, Location start, Tokenizer t, StringBuilder content) {
        if (t.atEnd()) {
            final var diag = new Diagnostic();
            diag.highlight(new Span(t.source(), start, t.location()));
            diag.note("invalid escape, expected something after '\\'");
            throw new TokenizeException(diag);
        }

        var c = t.take();
        if (c == '\\') {
            content.append('\\');
        } else if (c == 'n') {
            content.append('\n');
        } else if (c == 'r') {
            content.append('\r');
        } else if (c == 't') {
            content.append('\t');
        } else if (c == '"' && quote == '"') {
            content.append('"');
        } else if (c == '\'' && quote == '\'') {
            content.append('\'');
        } else {
            final var diag = new Diagnostic();
            diag.highlight(new Span(t.source(), t.location()));
            diag.note("invalid escape " + Text.repr(c));
            throw new TokenizeException(diag);
        }
    }
}