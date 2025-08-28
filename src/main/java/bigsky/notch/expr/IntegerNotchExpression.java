package bigsky.notch.expr;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class IntegerNotchExpression extends NotchExpression {
    public final Token token;

    public IntegerNotchExpression(Token token) {
        super(token.start, token.end);
        this.token = token;
    }

    private Integer value;
    public int value() {
        if (value == null) {
            value = token.integer();
        }
        return value;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return value();
    }
}
