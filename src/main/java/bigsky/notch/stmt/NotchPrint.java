package bigsky.notch.stmt;

import bigsky.notch.Location;
import bigsky.notch.expr.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;

public class NotchPrint extends NotchStatement {

    NotchExpression expr;

    public NotchPrint(Location start, Location end) {
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
