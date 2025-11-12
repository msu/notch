package bigsky.notch.statements;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

import java.util.Objects;

public class NotchPrint extends NotchStatement {

    NotchExpression expr;

    public NotchPrint(Location start, NotchExpression expr, Location end) {
        super(expr.fileId, start, end);
        this.expr = Objects.requireNonNull(expr);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object result = runtime.evaluate(expr);
        runtime.println(result);
    }
}
