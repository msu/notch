package edu.montana.notch.chisel.type;

<<<<<<<< HEAD:src/main/java/edu/montana/notch/chisel/type/TokenTypeString.java
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.util.Text;
========
import bigsky.notch.chisel.*;
import bigsky.notch.util.Text;
>>>>>>>> 00d0fae (new tokenizer api):src/main/java/edu/montana/notch/chisel/type/CStringTokenType.java

public class CStringTokenType implements TokenType {
    public static final CStringTokenType STR = new CStringTokenType();

    protected CStringTokenType() {}

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
