package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchIsNullExpression extends NotchExpression {
    public final NotchExpression expression;
    public final boolean isInverted;

    public NotchIsNullExpression(NotchExpression expression, boolean isInverted, Token end) {
        super(expression.span.through(end));
        this.expression = expression;
        this.isInverted = isInverted;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var value = runtime.evaluate(expression);
        return isInverted == (value != null);
    }
}
