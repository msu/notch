package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;
import java.util.Objects;

public class NotchIf extends NotchStatement {

    public final NotchExpression expr;
    public final List<NotchStatement> ifTrue;
    public final List<NotchStatement> ifFalse;

    public NotchIf(Span span, NotchExpression expr, List<NotchStatement> ifTrue, List<NotchStatement> ifFalse) {
        super(span);
        this.expr = Objects.requireNonNull(expr);
        this.ifTrue = List.copyOf(ifTrue);
        this.ifFalse = List.copyOf(ifFalse);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object result = runtime.evaluate(expr);
        if (Boolean.TRUE.equals(result)) {
            for (NotchStatement stmt : ifTrue) {
                runtime.execute(stmt);
            }
        } else {
            for (NotchStatement stmt : ifFalse) {
                runtime.execute(stmt);
            }
        }
    }
}
