package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;

public class NotchAdditiveExpression extends NotchExpression {
    private final NotchExpression lhs;
    private final NotchExpression rhs;

    public NotchAdditiveExpression(NotchExpression lhs, NotchExpression rhs) {
        super(lhs.start, rhs.end);
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object lhsVal = lhs.evaluate(runtime);
        Object rhsVal = rhs.evaluate(runtime);
        // TODO how do we want to handle numbers?
        if(lhsVal instanceof Number n1 && rhsVal instanceof Number n2) {
            return n1.intValue() + n2.intValue();
        } else {
            return String.valueOf(lhsVal) + rhsVal;
        }
    }
}
