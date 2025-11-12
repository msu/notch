package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.BetterMath;

public class NotchMultiplicationExpression extends NotchExpression {
    public final NotchExpression lhs, rhs;

    public NotchMultiplicationExpression(NotchExpression lhs, NotchExpression rhs) {
        super(lhs.fileId, lhs.start, rhs.end);
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var lhsVal = runtime.evaluate(lhs);
        var rhsVal = runtime.evaluate(rhs);

        if (lhsVal instanceof Number l && rhsVal instanceof Number r) {
            return BetterMath.safeMul(l, r);
        } else {
            throw runtime.raise(span(), "unable to multiply %s with %s".formatted(lhsVal, rhsVal));
        }
    }
}
