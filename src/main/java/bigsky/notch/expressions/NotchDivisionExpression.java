package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.utils.BetterMath;

public class NotchDivisionExpression extends NotchExpression {
    public final NotchExpression lhs, rhs;

    public NotchDivisionExpression(NotchExpression lhs, NotchExpression rhs) {
        super(lhs.fileId, lhs.start, rhs.end);
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var lhsVal = runtime.evaluate(lhs);
        var rhsVal = runtime.evaluate(rhs);

        if (lhsVal instanceof Number l && rhsVal instanceof Number r) {
            return BetterMath.safeDiv(l, r);
        } else {
            throw runtime.raise(span(), "unable to divide %s with %s".formatted(lhsVal, rhsVal));
        }
    }
}
