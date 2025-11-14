package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchDiagnostic;
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

        if (lhsVal instanceof Number lv && rhsVal instanceof Number rv) {
            return BetterMath.safeAdd(lv, rv);
        }

        var diag = new NotchDiagnostic();
        diag.highlight(fileId, span());
        diag.note("cannot add %s to %s".formatted(rhsVal.getClass().getName(), lhsVal.getClass().getName()));
        throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
    }
}
