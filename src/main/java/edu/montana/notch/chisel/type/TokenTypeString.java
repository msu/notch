package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.util.Text;

public class TokenTypeString implements TokenType {
    public static final TokenTypeString STR = new TokenTypeString();

    protected TokenTypeString() {}

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
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
                    throw new TokenizeException(start, t.location(), "invalid escape, expected something after '\\'");
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
                    throw new TokenizeException(t.location(), "invalid escape " + Text.repr(c));
                }
            }
        }

        if (!t.take(quote)) {
            throw new TokenizeException(start, t.location(), "unterminated string, expected the quote character " + quote);
        }

        return new Token(start, t.location(), this, content.toString());
    }
}
