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
        int index = 0;
        try (var scope = runtime.pushScope()) {
            Iterable i = runtime.coerce(Iterable.class, result, expr);
            for (Object o : i) {
                runtime.defineOrUpdate(((String) loopVariable.data), o);
                if (indexVariable != null) {
                    runtime.defineOrUpdate(indexVariable.str(), index++);
                }
                if (runBody(runtime) == Control.BREAK) return;
            }
        }
    }
}
