package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchDecimalExpression extends NotchExpression {
    public final Token token;

    public NotchDecimalExpression(Token token) {
        super(token.span);
        this.token = token;
    }

    // either a Double or a Float, depending on the literal's suffix
    public Number value() {
        return (Number) token.data;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return value();
    }
}
