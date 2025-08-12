package bigsky.notch.expr;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.NotchToken;

public class BooleanNotchExpression extends NotchExpression {
    public final NotchToken token;

    public BooleanNotchExpression(NotchToken token) {
        super(token.start, token.end);
        this.token = token;
    }

    private Boolean value;
    public boolean value() {
        if (value == null) {
            value = Boolean.valueOf(token.lex);
        }
        return value;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return value();
    }
}
