package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class NotchContainsExpression extends NotchExpression {
    public final NotchExpression lhs;
    public final NotchExpression rhs;

    public NotchContainsExpression(NotchExpression lhs, NotchExpression rhs) {
        super(lhs.span.through(rhs));
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object lval = runtime.evaluate(lhs);
        Object rval = runtime.evaluate(rhs);
        if (lval == null || runtime.isUndefined(lval)) return false;
        if (lval instanceof String s) {
            if (rval == null || runtime.isUndefined(rval)) return false;
            return s.contains(String.valueOf(rval));
        }
        if (lval instanceof Collection<?> c) return c.contains(rval);
        if (lval instanceof Object[] arr) return Arrays.asList(arr).contains(rval);
        if (lval instanceof Map<?, ?> m) return m.containsKey(rval);
        return false;
    }
}
