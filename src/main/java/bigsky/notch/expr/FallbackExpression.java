package bigsky.notch.expr;

import bigsky.notch.NotchRuntime;

public class FallbackExpression extends NotchExpression {
    public final NotchExpression value, fallback;

    public FallbackExpression(NotchExpression value, NotchExpression fallback) {
        super(value.start, fallback.end);
        this.value = addChild(value);
        this.fallback = addChild(fallback);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var value = this.value.evaluate(runtime);
        if (runtime.isUndefined(value)) {
            value = fallback.evaluate(runtime);
        }
        return value;
    }
}
