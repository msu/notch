package edu.montana.notch.token;

import edu.montana.notch.chisel.*;
import edu.montana.notch.util.BetterList;

public class TerseStringTokenType implements TokenType {
    public static final TerseStringTokenType TERSE_STRING = new TerseStringTokenType();

    protected TerseStringTokenType() {
    }

    protected boolean isTerseCharacter(char c) {
        return c != ' ' && c != ')' && c != ',' && c != ']' && c != '}' && c != '\0' && c != '\r' && c != '\n';
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        if (!t.take(":") || !isTerseCharacter(t.peek())) {
            return null;
        }

        var pieces = new BetterList<>();
        var content = new StringBuilder();
        while (isTerseCharacter(t.peek())) {
            char c = t.take();
            if (c != '{' || t.take('{')) {
                content.append(c);
                continue;
            }

            if (!content.isEmpty()) {
                pieces.add(content.toString());
                content.setLength(0);
            }
            var expr = FStringTokenData.tokenizeFExpression(t);
            pieces.add(expr);
        }
        if (!content.isEmpty()) {
            pieces.add(content.toString());
        }

        return new TokenData(new FStringTokenData(pieces));
    }
}
