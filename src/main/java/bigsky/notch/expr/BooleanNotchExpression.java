package bigsky.notch.expr;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class BooleanNotchExpression extends NotchExpression {
    public final Token token;

    public BooleanNotchExpression(Token token) {
        super(token.start, token.end);
        this.token = token;
    }

    private Boolean value;
    public boolean value() {
        if (value == null) value = token.bool();
        return value;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return value();
    }
}
