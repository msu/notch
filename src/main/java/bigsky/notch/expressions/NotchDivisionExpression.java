package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchDiagnostic;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.utils.BetterMath;
import bigsky.utils.Text;

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
            var diag = new NotchDiagnostic();
            diag.highlight(fileId, span());
            diag.note("cannot take the quotient of %s from %s".formatted(Text.className(rhsVal), Text.className(lhsVal)));
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
    }
}
