package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.BetterMath;
import bigsky.utils.chisel.Location;

import java.util.Objects;

public class NotchNegateExpression extends NotchExpression {
    public final NotchExpression expression;

    public NotchNegateExpression(Location start, NotchExpression expression, Location end) {
        super(expression.fileId, start, end);
        this.expression = Objects.requireNonNull(expression);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var val = runtime.evaluate(expression);
        if(val instanceof Number n) {
            return BetterMath.safeMul(-1, n);
        } else {
            throw runtime.raise(expression.span(), "Not a number: " + val);
        }
    }
}
