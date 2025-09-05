package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class IsEmptyExpression extends NotchExpression {
    public final NotchExpression lhs;
    public final boolean isInverted;

    public IsEmptyExpression(NotchExpression lhs, boolean isInverted, Token end) {
        super(lhs.start, end.end);
        this.lhs = lhs;
        this.isInverted = isInverted;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var lval = lhs.evaluate(runtime);
        if (lval instanceof String s) {
            return isInverted == s.isEmpty();
        } else {
            var iterable = runtime.asIterable(lhs.span(), lval);
            var it = iterable.iterator();
            return isInverted == it.hasNext();
        }
    }
}
