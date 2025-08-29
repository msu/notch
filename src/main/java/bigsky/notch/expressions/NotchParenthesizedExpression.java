package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

public class NotchParenthesizedExpression extends NotchExpression {

    private NotchExpression expression;

    public NotchParenthesizedExpression(Location start, Location end) {
        super(start, end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return expression.evaluate(runtime);
    }

    public void setExpression(NotchExpression expr) {
        this.expression = addChild(expr);
    }
}
