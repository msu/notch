package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchControlFlow;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchReturnIfStatement extends NotchStatement {
    private final NotchExpression condition;

    public NotchReturnIfStatement(Span span, NotchExpression condition) {
        super(span);
        this.condition = condition;
    }

    @Override
    public void execute(NotchRuntime runtime) {
        var result = runtime.evaluate(condition);
        var resultBool = runtime.coerce(Boolean.class, result, condition.span);
        if (resultBool) {
            throw new NotchControlFlow.Return(NotchRuntime.UNDEFINED);
        }
    }
}
