package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.token.FStringTokenData;

public class NotchFStringExpression extends NotchExpression {
    public final Token token;

    public NotchFStringExpression(Token token) {
        super(token.span);
        this.token = token;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var data = (FStringTokenData) token.data;
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
