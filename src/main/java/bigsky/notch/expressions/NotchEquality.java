package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;

import java.util.Objects;

public class NotchEquality extends NotchExpression {
    public final NotchExpression lhs, rhs;

    public NotchEquality(NotchExpression lhs, NotchExpression rhs) {
        super(lhs.start, rhs.end);
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var lv = lhs.evaluate(runtime);
        var rv = rhs.evaluate(runtime);
        return Objects.equals(lv, rv);
    }
}
