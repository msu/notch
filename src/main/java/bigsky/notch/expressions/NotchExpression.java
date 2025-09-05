package bigsky.notch.expressions;

import bigsky.notch.NotchElement;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

import java.util.Map;

public abstract class NotchExpression extends NotchElement {

    public NotchExpression(Location start, Location end) {
        super(start, end);
    }

    public abstract Object evaluate(NotchRuntime runtime);

    public final Object evaluate() {
        return evaluate(Map.of());
    }

    public final Object evaluate(Map<String, Object> symbols) {
        return evaluate(new NotchRuntime(symbols));
    }

    protected <T extends NotchExpression> T addChild(T expr) {
        return super.addChild(expr);
    }
}
