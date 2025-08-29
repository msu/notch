package bigsky.notch.statements;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

import java.util.ArrayList;
import java.util.List;

public class NotchIf extends NotchStatement {

    NotchExpression expr;
    List<NotchStatement> ifTrue = new ArrayList<>();
    List<NotchStatement> ifFalse = new ArrayList<>();

    public NotchIf(Location start, Location end) {
        super(start, end);
    }

    public void setExpression(NotchExpression expr) {
        this.expr = expr;
    }

    public void addTrueStatement(NotchStatement statement) {
        ifTrue.add(addChild(statement));
    }

    public void addFalseStatement(NotchStatement statement) {
        ifFalse.add(addChild(statement));
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
