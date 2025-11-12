package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

import java.util.Objects;

public class NotchParenthesizedExpression extends NotchExpression {

    public final NotchExpression expression;

    public NotchParenthesizedExpression(Location start, NotchExpression expression, Location end) {
        super(expression.fileId, start, end);
        this.expression = Objects.requireNonNull(expression);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return runtime.evaluate(expression);
    }
}
