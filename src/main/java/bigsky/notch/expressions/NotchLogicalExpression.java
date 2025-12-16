package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class NotchLogicalExpression extends NotchExpression {
    private final Token op;
    private NotchExpression lhs;
    private NotchExpression rhs;

    public NotchLogicalExpression(Token op, NotchExpression lhs, NotchExpression rhs) {
        super(lhs.fileId, lhs.start, rhs.end);
        this.op = op;
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object lhsValue = runtime.evaluate(lhs);
        if(op.str().equals("and") || op.str().equals("&&")) {
            if(!runtime.isTruthy(lhsValue)) {
                return false;
            }
            Object rhsValue = runtime.evaluate(rhs);
            return runtime.isTruthy(rhsValue);
        } else {
            if(runtime.isTruthy(lhsValue)) {
                return true;
            }
            Object rhsValue = runtime.evaluate(rhs);
            return runtime.isTruthy(rhsValue);
        }
    }
}
