package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;

public class NotchEndsWithExpression extends NotchExpression {

    public final NotchExpression lhs;
    public final NotchExpression rhs;

    public NotchEndsWithExpression(NotchExpression lhs, NotchExpression rhs) {
        super(lhs.span.through(rhs));
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object lval = runtime.evaluate(lhs);
        Object rval = runtime.evaluate(rhs);
        if (lval == null || runtime.isUndefined(lval)) return false;
        if (rval == null || runtime.isUndefined(rval)) return false;
        return String.valueOf(lval).endsWith(String.valueOf(rval));
    }
}
