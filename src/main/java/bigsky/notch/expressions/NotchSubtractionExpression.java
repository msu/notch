package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchDiagnostic;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.utils.BetterMath;

public class NotchSubtractionExpression extends NotchExpression {
    public final NotchExpression lhs, rhs;

    public NotchSubtractionExpression(NotchExpression lhs, NotchExpression rhs) {
        super(lhs.fileId, lhs.start, rhs.end);
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var lhsVal = runtime.evaluate(lhs);
        var rhsVal = runtime.evaluate(rhs);

        if (lhsVal instanceof Number l && rhsVal instanceof Number r) {
            return BetterMath.safeSub(l, r);
        } else {
            var diag = new NotchDiagnostic();
            diag.highlight(fileId, span());
            diag.note("cannot subtract %s from %s".formatted(rhsVal.getClass().getName(), lhsVal.getClass().getName()));
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
    }
}
