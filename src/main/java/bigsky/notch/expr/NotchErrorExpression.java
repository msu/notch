package bigsky.notch.expr;

import bigsky.utils.chisel.Location;

public abstract class NotchErrorExpression extends NotchExpression {
    public final String message;
    public final NotchExpression child;
    public final Throwable cause;


    public NotchErrorExpression(String message, NotchExpression expr) {
        super(expr.start, expr.end);
        this.message = message;
        this.child = expr;
        this.cause = null;
    }

    public NotchErrorExpression(Location start, Location end, Throwable cause) {
        super(start, end);
        this.message = cause.getMessage();
        this.child = null;
        this.cause = cause;
    }
}
