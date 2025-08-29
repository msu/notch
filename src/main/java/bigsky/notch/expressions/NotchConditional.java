package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;

public class NotchConditional extends NotchExpression {
    public final NotchExpression value, condition, fallback;

    public NotchConditional(NotchExpression value, NotchExpression condition, NotchExpression fallback) {
        super(value.start, fallback == null ? condition.end : fallback.end);
        this.value = addChild(value);
        this.condition = addChild(condition);
        this.fallback = addChild(fallback);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var cond = condition.evaluate(runtime);
        if (runtime.isTruthy(cond)) {
            return value.evaluate(runtime);
        }
        if (fallback == null) {
            return NotchRuntime.UNDEFINED;
        }
        return fallback.evaluate(runtime);
    }
}
