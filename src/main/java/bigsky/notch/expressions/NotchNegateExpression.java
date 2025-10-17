package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.utils.BetterMath;
import bigsky.utils.chisel.Location;

public class NotchNegateExpression extends NotchExpression {
    private NotchExpression expression;

    public NotchNegateExpression(Location start, Location end) {
        super(start, end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var val = expression.evaluate(runtime);
        if(val instanceof Number n) {
            return BetterMath.safeMul(-1, n);
        } else {
            throw new NotchRuntimeException(expression.span(), "Not a number: " + val);
        }
    }

    public void setExpression(NotchExpression expr) {
        this.expression = addChild(expr);
    }
}
