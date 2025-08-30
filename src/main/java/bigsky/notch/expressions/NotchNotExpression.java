package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

public class NotchNotExpression extends NotchExpression {
    private NotchExpression expression;

    public NotchNotExpression(Location start, Location end) {
        super(start, end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object result = expression.evaluate(runtime);
        return !runtime.isTruthy(result);
    }

    public void setExpression(NotchExpression expr) {
        this.expression = addChild(expr);
    }
}
