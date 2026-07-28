package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;

public class NotchNotNullExpression extends NotchExpression {
    public final NotchExpression expression;

    public NotchNotNullExpression(NotchExpression expression, Token lastToken) {
        super(expression.span().through(lastToken));
        this.expression = expression;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var value = runtime.evaluate(expression);
        if (runtime.isUndefined(value) || value == null) {
            var diag = new Diagnostic()
                    .highlight(expression)
                    .note("this expression was " + value);
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
        return value;
    }
}
