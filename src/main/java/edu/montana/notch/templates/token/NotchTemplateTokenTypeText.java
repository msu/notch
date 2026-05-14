package edu.montana.notch.templates.token;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.Tokenizer;

public class NotchTemplateTokenTypeText implements TokenType {
    public static final NotchTemplateTokenTypeText TEXT = new NotchTemplateTokenTypeText();

    private NotchTemplateTokenTypeText() {
    }

    @Override
    public Token tokenize(Tokenizer t) {
        var start = t.location();

        var content = new StringBuilder();
        while (!t.atEnd()) {
            if (t.peek("${")) break;
//            if (t.take("\\$")) {  // maybe...
//                content.append("$");
//                continue;
//            }
            char c = t.take();
            content.append(c);
            if (c == '\n') {
                break;
            }
        }

        String data = content.toString();
        return new Token(start, t.location(), this, data);
    }
}
