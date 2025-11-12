package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class NotchComparisonExpression extends NotchExpression {
    private final Token op;
    private NotchExpression lhs;
    private NotchExpression rhs;
    public NotchComparisonExpression(Token op, NotchExpression lhs, NotchExpression rhs) {
        super(lhs.fileId, lhs.start, rhs.end);
        this.op = op;
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Integer lhsValue = (Integer) runtime.evaluate(lhs);
        Integer rhsValue = (Integer) runtime.evaluate(rhs);
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
