package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchControlFlow;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchReturnStatement extends NotchStatement {
    private final NotchExpression value;

    public NotchReturnStatement(Span span, NotchExpression value) {
        super(span);
        this.value = addChild(value);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object v = null;
        if (value != null) v = runtime.evaluate(value);
        throw new NotchControlFlow.Return(v);
    }
}
