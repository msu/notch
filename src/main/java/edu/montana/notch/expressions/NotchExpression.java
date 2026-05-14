package edu.montana.notch.expressions;

import edu.montana.notch.NotchElement;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.chisel.Location;

import java.util.Map;

public abstract class NotchExpression extends NotchElement {

    public NotchExpression(String fileId, Location start, Location end) {
        super(fileId, start, end);
    }

    public abstract Object evaluate(NotchRuntime runtime);

    public final Object evaluate() {
        return evaluate(Map.of());
    }

    public final Object evaluate(Map<String, Object> symbols) {
        var runtime = new NotchRuntime(fileId, symbols);
        return runtime.evaluate(this);
    }

    protected <T extends NotchExpression> T addChild(T expr) {
        return super.addChild(expr);
    }
}
