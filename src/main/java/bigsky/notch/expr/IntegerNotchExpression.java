package bigsky.notch.expr;

import bigsky.notch.NotchRuntime;
import bigsky.notch.NotchToken;

public class IntegerNotchExpression extends NotchExpression {
    public final NotchToken token;

    public IntegerNotchExpression(NotchToken token) {
        super(token.start, token.end);
        this.token = token;
    }

    private Integer value;
    public int value() {
        if (value == null) {
            value = Integer.parseInt(token.lex);
        }
        return value;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return value();
    }
}
