package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.utils.BetterMath;

public class NotchAdditiveExpression extends NotchExpression {
    private final NotchExpression lhs;
    private final NotchExpression rhs;

    public NotchAdditiveExpression(NotchExpression lhs, NotchExpression rhs) {
        super(lhs.fileId, lhs.start, rhs.end);
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    // this is tricky cuz there are a lot of types of numbers
    // we need to efficiently and losslessly (relatively) convert between them
    // that means we have to handle decimals first, and then integers and utilize a greatest type or a universal type..
    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object lhsVal = runtime.evaluate(lhs);
        Object rhsVal = runtime.evaluate(rhs);

        if (lhsVal instanceof String || rhsVal instanceof String) {
            return String.valueOf(lhsVal) + rhsVal;
        }

        if (lhsVal instanceof Number lv) {
            var rv = runtime.asNumber(rhsVal);
            return BetterMath.safeAdd(lv, rv);
        }

        throw runtime.raise(span(), "cannot add " + lhsVal + " and " + rhsVal);
    }
}
