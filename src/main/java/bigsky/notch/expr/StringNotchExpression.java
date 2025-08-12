package bigsky.notch.expr;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.NotchToken;

public class StringNotchExpression extends NotchExpression {
    public final NotchToken token;

    public StringNotchExpression(NotchToken token) {
        super(token.start, token.end);
        this.token = token;
    }

    public String value() {
        return token.lex;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return value();
    }
}
