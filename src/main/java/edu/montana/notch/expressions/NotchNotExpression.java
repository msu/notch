package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchNotExpression extends NotchExpression {
    public final NotchExpression expression;

    public NotchNotExpression(Span span, NotchExpression expression) {
        super(span);
        this.expression = expression;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object result = runtime.evaluate(expression);
        return !runtime.isTruthy(result);
    }
}
