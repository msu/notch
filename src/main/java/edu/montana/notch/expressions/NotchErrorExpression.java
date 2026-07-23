package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;

public class NotchErrorExpression extends NotchExpression {
    public final Diagnostic diagnostic;
    public final NotchExpression child;

    public NotchErrorExpression(String message, NotchExpression expr) {
        super(expr.span);
        this.diagnostic = new Diagnostic().setTitle(message).highlight(expr);
        this.child = expr;
    }

    public NotchErrorExpression(Span span, Throwable cause) {
        super(span);
        this.diagnostic = new Diagnostic().setTitle(cause.getMessage()).highlight(span);
        this.child = null;
    }

    public NotchErrorExpression(Span span, Diagnostic diagnostic) {
        super(span);
        this.diagnostic = diagnostic;
        this.child = null;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        throw new NotchRuntimeException(runtime.currentStackTrace(), diagnostic);
    }
}
