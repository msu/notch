package bigsky.notch.expr;

import bigsky.notch.NotchRuntime;

public class ConditionalExpression extends NotchExpression {
    public final NotchExpression value, condition, fallback;

    public ConditionalExpression(NotchExpression value, NotchExpression condition, NotchExpression fallback) {
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
