package edu.montana.notch.expressions;

import edu.montana.notch.NotchElement;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.Map;

public abstract class NotchExpression extends NotchElement {

    public NotchExpression(Span span) {
        super(span);
    }

    public abstract Object evaluate(NotchRuntime runtime);

    public final Object evaluate() {
        return evaluate(Map.of());
    }

    public final Object evaluate(Map<String, Object> symbols) {
        var runtime = new NotchRuntime(span.source(), symbols);
        return runtime.evaluate(this);
    }

    protected <T extends NotchExpression> T addChild(T expr) {
        return super.addChild(expr);
    }
}
