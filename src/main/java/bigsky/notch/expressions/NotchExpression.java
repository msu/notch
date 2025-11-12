package bigsky.notch.expressions;

import bigsky.notch.NotchElement;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

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
