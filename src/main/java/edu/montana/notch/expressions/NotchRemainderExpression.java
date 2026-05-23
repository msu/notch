package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.util.BetterMath;

public class NotchRemainderExpression extends NotchExpression {
    public final NotchExpression lhs, rhs;

    public NotchRemainderExpression(NotchExpression lhs, NotchExpression rhs) {
        super(lhs.span.through(rhs));
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var lhsVal = runtime.evaluate(lhs);
        var rhsVal = runtime.evaluate(rhs);

        if (lhsVal instanceof Number l && rhsVal instanceof Number r) {
            return BetterMath.safeRem(l, r);
        } else {
            var diag = new Diagnostic();
            diag.highlight(span());
            diag.note("cannot take the remainder of %s from %s".formatted(NotchRuntime.className(lhsVal), NotchRuntime.className(rhsVal)));
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
    }
}
