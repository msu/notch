package bigsky.notch.expr;

import bigsky.notch.Location;
import bigsky.notch.NotchElement;
import bigsky.notch.runtime.NotchRuntime;

import java.beans.Expression;
import java.util.*;

public abstract class NotchExpression extends NotchElement {

    public NotchExpression(Location start, Location end) {
        super(start, end);
    }

    protected <T extends NotchExpression> T addChild(T expr) {
        return super.addChild(expr);
    }

    public final Object evaluate() {
        return evaluate(Map.of());
    }

    public final Object evaluate(Map<String, Object> symbols) {
        return evaluate(new NotchRuntime(symbols));
    }

    public abstract Object evaluate(NotchRuntime runtime);
}
