package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Span;

public abstract class NotchErrorExpression extends NotchExpression {
    public final String message;
    public final NotchExpression child;
    public final Throwable cause;


    public NotchErrorExpression(String message, NotchExpression expr) {
        super(expr.span);
        this.message = message;
        this.child = expr;
        this.cause = null;
    }

    public NotchErrorExpression(Span span, Throwable cause) {
        super(span);
        this.message = cause.getMessage();
        this.child = null;
        this.cause = cause;
    }
}
