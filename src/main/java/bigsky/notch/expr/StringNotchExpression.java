package bigsky.notch.expr;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class StringNotchExpression extends NotchExpression {
    public final Token token;

    public StringNotchExpression(Token token) {
        super(token.start, token.end);
        this.token = token;
    }

    public String value() {
        return token.str();
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return value();
    }
}
