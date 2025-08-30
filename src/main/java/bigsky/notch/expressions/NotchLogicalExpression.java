package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class NotchLogicalExpression extends NotchExpression {
    private final Token op;
    private NotchExpression lhs;
    private NotchExpression rhs;
    public NotchLogicalExpression(Token op, NotchExpression lhs, NotchExpression rhs) {
        super(lhs.start, rhs.end);
        this.op = op;
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object lhsValue = lhs.evaluate(runtime);
        Object rhsValue = rhs.evaluate(runtime);
        if(op.str().equals("and") || op.str().equals("&&")) {
            return runtime.isTruthy(lhsValue) && runtime.isTruthy(rhsValue);
        } else {
            return runtime.isTruthy(lhsValue) || runtime.isTruthy(rhsValue);
        }
    }
}
