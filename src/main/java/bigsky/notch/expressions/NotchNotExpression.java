package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.chisel.Location;

import java.util.Objects;

public class NotchNotExpression extends NotchExpression {
    public final NotchExpression expression;

    public NotchNotExpression(Location start, NotchExpression expression, Location end) {
        super(expression.fileId, start, end);
        this.expression = Objects.requireNonNull(expression);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object result = runtime.evaluate(expression);
        return !runtime.isTruthy(result);
    }
}
