package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchClosure;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.statements.NotchStatement;
import bigsky.utils.chisel.Location;
import bigsky.utils.chisel.Token;

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
        NotchRuntime closure = runtime.captureClosure(fileId, span());
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
