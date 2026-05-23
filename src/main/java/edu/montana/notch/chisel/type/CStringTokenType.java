package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.*;
import edu.montana.notch.util.Text;

public class CStringTokenType implements TokenType {
    public static final CStringTokenType STR = new CStringTokenType();

    protected CStringTokenType() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();

        if (!t.peek('"', '\'')) return null;
        char quote = t.take();

        var content = new StringBuilder();
        while (!t.atEnd()) {
            char c = t.peek();
            if (c == quote || c == '\n') {
                break;
            }

            t.take();
            if (c != '\\') {
                content.append(c);
            } else {
                if (t.atEnd()) {
                    final var diag = new Diagnostic();
                    diag.highlight(new Span(t.source(), start, t.location()));
                    diag.note("invalid escape, expected something after '\\'");
                    throw new TokenizeException(diag);
                }

                c = t.take();
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

        if (!t.take(quote)) {
            final var diag = new Diagnostic();
            diag.highlight(new Span(t.source(), t.location(), t.location()));
            diag.note("unterminated string, expected the quote character " + quote);
            throw new TokenizeException(diag);
        }

        return new TokenData(content.toString());
    }
}
