package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.chisel.Token;

public class IsEmptyExpression extends NotchExpression {
    public final NotchExpression lhs;
    public final boolean isInverted;

    public IsEmptyExpression(NotchExpression lhs, boolean isInverted, Token end) {
        super(lhs.fileId, lhs.start, end.end);
        this.lhs = lhs;
        this.isInverted = isInverted;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var lval = runtime.evaluate(lhs);
        if (lval instanceof String s) {
            return isInverted == s.isEmpty();
        } else {
            var iterable = runtime.coerceIterable(lhs.fileId, lhs.span(), lval);
            var it = iterable.iterator();
            return isInverted == it.hasNext();
        }
    }
}
