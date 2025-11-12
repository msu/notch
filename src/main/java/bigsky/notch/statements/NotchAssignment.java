package bigsky.notch.statements;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;
import bigsky.utils.chisel.Token;

public class NotchAssignment extends NotchStatement {
    private Token name;
    private NotchExpression expression;

    public NotchAssignment(String fileId, Location start, Location end) {
        super(fileId, start, end);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object value = runtime.evaluate(expression);
        runtime.defineOrUpdate(name.str(), value);
    }

    public void setVariableName(Token varName) {
        this.name = varName;
    }

    public void setExpression(NotchExpression valueExpression) {
        this.expression = valueExpression;
    }
}
