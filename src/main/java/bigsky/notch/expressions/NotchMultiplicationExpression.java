package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchDiagnostic;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.utils.BetterMath;
import bigsky.utils.Text;

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
            var diag = new NotchDiagnostic();
            diag.highlight(fileId, span());
            diag.note("cannot take the product of %s from %s".formatted(Text.className(lhsVal), Text.className(rhsVal)));
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
    }
}
