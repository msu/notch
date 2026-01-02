package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.BetterMath;
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
        Object lhsValue = runtime.evaluate(lhs);
        Object rhsValue = runtime.evaluate(rhs);
        if(lhsValue instanceof Number lhsNumber && rhsValue instanceof Number rhsNumber) {
            BetterMath.Ordering ordering = BetterMath.safeCompare(lhsNumber, rhsNumber);
            if (op.str().equals(">")) {
                return ordering == BetterMath.Ordering.GreaterThan;
            } else if (op.str().equals("<")) {
                return ordering == BetterMath.Ordering.LessThan;
            } else if (op.str().equals("<=")) {
                return ordering == BetterMath.Ordering.LessThan ||  ordering == BetterMath.Ordering.Equal;
            } else {
                return ordering == BetterMath.Ordering.GreaterThan ||  ordering == BetterMath.Ordering.Equal;
            }
        } else {
            return false;
        }
    }
}
