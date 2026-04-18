package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchClosure;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.statements.NotchStatement;
import bigsky.notch.chisel.Location;
import bigsky.notch.chisel.Token;

import java.util.List;

public class NotchClosureExpression extends NotchExpression {
    private List<Token> parameters;
    private NotchExpression expression;
    private List<NotchStatement> statements;

    public NotchClosureExpression(String fileId, Location start, Location end) {
        super(fileId, start, end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        NotchRuntime closure = new NotchRuntime(fileId, runtime);
        return new NotchClosure(closure, parameters, expression, statements);
    }

    public void setParameters(List<Token> params) {
        this.parameters = params;
    }

    public void setExpression(NotchExpression expression) {
        this.expression = addChild(expression);
    }

    public void setStatements(List<NotchStatement> statements) {
        this.statements = addChildren(statements);
    }
}
