package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.utils.Numbers;

public class NotchRemainderExpression extends NotchExpression {
    public final NotchExpression lhs, rhs;

    public NotchRemainderExpression(NotchExpression lhs, NotchExpression rhs) {
        super(lhs.start, rhs.end);
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var lhsVal = lhs.evaluate(runtime);
        var rhsVal = rhs.evaluate(runtime);

        if (lhsVal instanceof Number l && rhsVal instanceof Number r) {
            return Numbers.safeRem(l, r);
        } else {
            throw new NotchRuntimeException(span(), "unable to subtract %s with %s".formatted(lhsVal, rhsVal));
        }
    }
}
