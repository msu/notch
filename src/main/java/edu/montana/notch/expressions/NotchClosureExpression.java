package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchClosure;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.statements.NotchStatement;

import java.util.List;

public class NotchClosureExpression extends NotchExpression {
    private final List<Token> parameters;
    private final NotchExpression expression;
    private final List<NotchStatement> statements;

    public NotchClosureExpression(Span span, List<Token> parameters, NotchExpression expression, List<NotchStatement> statements) {
        super(span);
        this.parameters = parameters;
        this.expression = addChild(expression);
        this.statements = statements;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        NotchRuntime closure = new NotchRuntime(source(), runtime);
        return new NotchClosure(closure, parameters, expression, statements, null);
    }
}
