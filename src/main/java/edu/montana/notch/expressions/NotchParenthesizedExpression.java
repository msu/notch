package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.Objects;

public class NotchParenthesizedExpression extends NotchExpression {

    public final NotchExpression expression;

    public NotchParenthesizedExpression(Span span, NotchExpression expression) {
        super(span);
        this.expression = Objects.requireNonNull(expression);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return runtime.evaluate(expression);
    }
}
