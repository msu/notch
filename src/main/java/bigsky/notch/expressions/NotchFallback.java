package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;

public class NotchFallback extends NotchExpression {
    public final NotchExpression primary, fallback;

    public NotchFallback(NotchExpression primary, NotchExpression fallback) {
        super(primary.fileId, primary.start, fallback.end);
        this.primary = addChild(primary);
        this.fallback = addChild(fallback);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object value;
        if (primary instanceof NotchIdentifier id) {
            value = runtime.getSymbol(id.name());
        } else {
            value = runtime.evaluate(primary);
        }
        if (runtime.isUndefined(value)) {
            value = runtime.evaluate(fallback);
        }
        return value;
    }
}
