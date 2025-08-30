package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class NotchComparisonExpression extends NotchExpression {
    private final Token op;
    private NotchExpression lhs;
    private NotchExpression rhs;
    public NotchComparisonExpression(Token op, NotchExpression lhs, NotchExpression rhs) {
        super(lhs.start, rhs.end);
        this.op = op;
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Integer lhsValue = (Integer) lhs.evaluate(runtime);
        Integer rhsValue = (Integer) rhs.evaluate(runtime);
        if (op.str().equals(">")) {
            return lhsValue > rhsValue;
        } else if (op.str().equals("<")) {
            return lhsValue < rhsValue;
        } else if (op.str().equals("<=")) {
            return lhsValue <= rhsValue;
        } else {
            return lhsValue >= rhsValue;}
    }
}
