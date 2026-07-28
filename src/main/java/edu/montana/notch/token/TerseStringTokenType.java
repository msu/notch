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
        if (t.take(':')) {
            var lex = new StringBuilder();
            while (isTerseCharacter(t.peek())) {
                char c = t.take();
                lex.append(c);
            }

            if (lex.isEmpty()) return null;

            return new TokenData(lex.toString());
        }

        if (t.take("f:")) {
            var pieces = new BetterList<>();
            var piece = new StringBuilder();
            while (isTerseCharacter(t.peek())) {
                char c = t.take();
                if (c != '{' || t.take('{')) {
                    piece.append(c);
                    continue;
                }

                if (!piece.isEmpty()) {
                    pieces.add(piece.toString());
                    piece.setLength(0);
                }
                var expr = FStringTokenData.tokenizeFExpression(t);
                pieces.add(expr);
            }
            if (!piece.isEmpty()) {
                pieces.add(piece.toString());
            }

            return new TokenData("fstring", new FStringTokenData(pieces));
        }

        return null;
    }
}
