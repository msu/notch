package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.statements.NotchCatch;
import edu.montana.notch.util.Exceptions;

import java.util.List;

public class NotchInlineCatch extends NotchExpression {
    private final NotchExpression tryExpr;
    private final List<NotchCatch> clauses;
    private final NotchExpression recoverExpr;

    public NotchInlineCatch(Span span, NotchExpression tryExpr, List<NotchCatch> clauses, NotchExpression recoverExpr) {
        super(span);
        this.tryExpr = addChild(tryExpr);
        this.clauses = clauses;
        for (NotchCatch c : clauses) addChildren(c.body);
        this.recoverExpr = recoverExpr == null ? null : addChild(recoverExpr);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        try {
            return runtime.evaluate(tryExpr);
        } catch (Throwable t) {
            if (NotchCatch.isControlFlow(t)) throw Exceptions.rethrow(t);
            return handle(runtime, t);
        }
    }

    private Object handle(NotchRuntime runtime, Throwable t) {
        try {
            NotchCatch.catchHandled(runtime, t, clauses);
        } catch (Throwable fromBody) {
            throw Exceptions.rethrow(fromBody);
        }
        if (recoverExpr != null) return runtime.evaluate(recoverExpr);
        throw Exceptions.rethrow(t);
    }
}
