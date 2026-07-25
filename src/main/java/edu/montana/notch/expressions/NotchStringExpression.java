package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.token.FStringTokenData;

public class NotchStringExpression extends NotchExpression {
    public final Token token;

    public NotchStringExpression(Token token) {
        super(token.span);
        this.token = token;
    }

    private String value;

    @Override
    public Object evaluate(NotchRuntime runtime) {
        if (value != null) {
            return value;
        }
        var data = (FStringTokenData) token.data;
        if (data.pieces().size() == 1 && data.pieces().get(0) instanceof String s) {
            value = s;
            return value;
        }

        var out = new StringBuilder();
        for (var piece : data.pieces()) {
            if (piece instanceof String s) {
                out.append(s);
            } else if (piece instanceof NotchExpression e) {
                var expr = e.evaluate(runtime);
                out.append(expr);
            } else {
                throw new IllegalStateException("unreachable");
            }
        }
        return out.toString();
    }
}
