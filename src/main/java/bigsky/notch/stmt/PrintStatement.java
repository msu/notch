package bigsky.notch.stmt;

import bigsky.notch.expr.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

public class PrintStatement extends NotchStatement {

    NotchExpression expr;

    public PrintStatement(Location start, Location end) {
        super(start, end);
    }

    public void setExpression(NotchExpression expr) {
        this.expr = addChild(expr);
    }


    @Override
    public void execute(NotchRuntime runtime) {
        Object result = expr.evaluate(runtime);
        runtime.println(result);
    }
}
