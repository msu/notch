package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class NotchString extends NotchExpression {
    public final Token token;

    public NotchString(Token token) {
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
