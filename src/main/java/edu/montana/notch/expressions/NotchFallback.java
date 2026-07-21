package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;

public class NotchFallback extends NotchExpression {
    public final NotchExpression primary, fallback;

    public NotchFallback(NotchExpression primary, NotchExpression fallback) {
        super(primary.span.through(fallback));
        this.primary = addChild(primary);
        this.fallback = addChild(fallback);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var value = runtime.evaluate(primary);
        if (runtime.isUndefined(value) || value == null) {
            value = runtime.evaluate(fallback);
        }
        return value;
    }
}
