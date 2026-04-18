package bigsky.notch.expressions;

import bigsky.notch.chisel.Span;

public abstract class NotchErrorExpression extends NotchExpression {
    public final String message;
    public final NotchExpression child;
    public final Throwable cause;


    public NotchErrorExpression(String message, NotchExpression expr) {
        super(expr.fileId, expr.start, expr.end);
        this.message = message;
        this.child = expr;
        this.cause = null;
    }

    public NotchErrorExpression(String fileId, Span span, Throwable cause) {
        super(fileId, span.start(), span.end());
        this.message = cause.getMessage();
        this.child = null;
        this.cause = cause;
    }
}
