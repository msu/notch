package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.UnknownVariableException;

public class NotchFallbackExpression extends NotchExpression {
    public final NotchExpression primary, fallback;

    public NotchFallbackExpression(NotchExpression primary, NotchExpression fallback) {
        super(primary.span.through(fallback));
        this.primary = addChild(primary);
        this.fallback = addChild(fallback);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object value;
        if (primary instanceof NotchIdentifierExpression || primary instanceof NotchFallbackExpression) {
            try {
                value = runtime.evaluate(primary);
            } catch (UnknownVariableException e) {
                value = NotchRuntime.UNDEFINED;
            }
        } else {
            value = runtime.evaluate(primary);
        }

        if (runtime.isUndefined(value) || value == null) {
            value = runtime.evaluate(fallback);
        }
        return value;
    }
}
