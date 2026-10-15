package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;
import java.util.Objects;

public class NotchForLoop extends NotchLoop {

    public final Token loopVariable;
    public final Token indexVariable;
    public final NotchExpression expr;

    public NotchForLoop(Span span, Token loopVariable, NotchExpression expr, Token indexVariable, List<NotchStatement> body) {
        super(span, body);
        this.loopVariable = Objects.requireNonNull(loopVariable);
        this.expr = Objects.requireNonNull(expr);
        this.indexVariable = indexVariable;
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object result = runtime.evaluate(expr);
        Iterable i = runtime.coerce(Iterable.class, result, expr);
        int index = 0;
        try (var scope = runtime.pushScope()) {
            for (Object o : i) {
                scope.define(loopVariable.str(), o);
                if (indexVariable != null) {
                    scope.define(indexVariable.str(), index++);
                }
                if (runBody(runtime) == Control.BREAK) return;
            }
        }
    }
}
