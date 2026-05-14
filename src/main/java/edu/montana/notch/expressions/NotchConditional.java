package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;

public class NotchConditional extends NotchExpression {
    public final NotchExpression value, condition, fallback;

    public NotchConditional(NotchExpression value, NotchExpression condition, NotchExpression fallback) {
        super(value.fileId, value.start, fallback == null ? condition.end : fallback.end);
        this.value = addChild(value);
        this.condition = addChild(condition);
        this.fallback = addChild(fallback);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var cond = runtime.evaluate(condition);
        if (runtime.isTruthy(cond)) {
            return runtime.evaluate(value);
        }
        if (fallback == null) {
            return NotchRuntime.UNDEFINED;
        }
        return runtime.evaluate(fallback);
    }
}
