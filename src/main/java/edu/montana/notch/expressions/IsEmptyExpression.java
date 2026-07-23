package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;

public class IsEmptyExpression extends NotchExpression {
    public final NotchExpression lhs;
    public final boolean isInverted;

    public IsEmptyExpression(NotchExpression lhs, boolean isInverted, Token end) {
        super(lhs.span.through(end.span));
        this.lhs = lhs;
        this.isInverted = isInverted;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var lval = runtime.evaluate(lhs);
        if (lval instanceof String s) {
            return isInverted != s.isEmpty();
        } else {
            var iterable = runtime.coerceIterable(lhs.span(), lval);
            var it = iterable.iterator();
            return isInverted == it.hasNext();
        }
    }
}
