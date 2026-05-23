package edu.montana.notch.statements;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchAssignment extends NotchStatement {
    private Token name;
    private NotchExpression expression;

    public NotchAssignment(Token name, NotchExpression expression) {
        super(name.span.through(expression));
        this.name = name;
        this.expression = expression;
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
