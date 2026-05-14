package edu.montana.notch.statements;

import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.chisel.Location;

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
