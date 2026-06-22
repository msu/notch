package edu.montana.notch.statements;

import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;

//wrap allows expressions producing side effects to appear in statement position
//i.e. print("hello"), myFunc(), myFunc() catch print() recover 0
public class NotchExpressionStatement extends NotchStatement {

    private final NotchExpression expression;

    public NotchExpressionStatement(NotchExpression expression) {
        super(expression.span);
        this.expression = addChild(expression);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        runtime.evaluate(expression);
    }
}
