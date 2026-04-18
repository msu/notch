package bigsky.notch.templates.token;

import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.Tokenizer;

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
