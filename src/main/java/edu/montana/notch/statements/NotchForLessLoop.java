package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;

public class NotchForLessLoop extends NotchLoop {
    private final NotchExpression expr;
    private final Token id;

    public NotchForLessLoop(Span span, Token id, NotchExpression expr, List<NotchStatement> body) {
        super(span, body);
        this.id = id;
        this.expr = expr;
    }

    @Override
    public void execute(NotchRuntime runtime) {
        try (var scope = runtime.pushScope()) {
            var value = runtime.evaluate(expr);
            var num = (int) runtime.coerce(Integer.class, value, expr);
            for (int i = 0; i < num; i++) {
                scope.define(id.str(), i);
                runBody(runtime);
            }
        }
    }
}
