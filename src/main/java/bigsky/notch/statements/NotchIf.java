package bigsky.notch.statements;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

import java.util.ArrayList;
import java.util.List;

public class NotchIf extends NotchStatement {

    NotchExpression expr;
    List<NotchStatement> ifTrue;
    List<NotchStatement> ifFalse;

    public NotchIf(Location start, Location end) {
        super(start, end);
    }

    public void setExpression(NotchExpression expr) {
        this.expr = addChild(expr);
    }

    public void setIfTrue(List<NotchStatement> ifTrue) {
        this.ifTrue = addChildren(ifTrue);
    }

    public void setIfFalse(List<NotchStatement> ifFalse) {
        this.ifFalse = addChildren(ifFalse);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object result = expr.evaluate(runtime);
        if(Boolean.TRUE.equals(result)) {
            for (NotchStatement stmt : ifTrue) {
                stmt.execute(runtime);
            }
        } else {
            for (NotchStatement stmt : ifFalse) {
                stmt.execute(runtime);
            }
        }
    }
}
