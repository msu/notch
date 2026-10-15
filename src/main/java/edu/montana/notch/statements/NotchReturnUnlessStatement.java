package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchControlFlow;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchReturnUnlessStatement extends NotchStatement {
    private final NotchExpression condition;

    public NotchReturnUnlessStatement(Span span, NotchExpression condition) {
        super(span);
        this.condition = condition;
    }

    @Override
    public void execute(NotchRuntime runtime) {
        var result = runtime.evaluate(condition);
        var resultBool = runtime.cast(Boolean.class, result, condition.span);
        if (!resultBool) {
            throw new NotchControlFlow.Return(NotchRuntime.UNDEFINED);
        }
    }
}
