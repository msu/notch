package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.Objects;

public class NotchPrint extends NotchStatement {

    NotchExpression expr;

    public NotchPrint(Span span, NotchExpression expr) {
        super(span);
        this.expr = Objects.requireNonNull(expr);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object result = runtime.evaluate(expr);
        runtime.println(result);
    }
}
