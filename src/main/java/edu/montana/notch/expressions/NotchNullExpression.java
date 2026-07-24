package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchNullExpression extends NotchExpression {
    public NotchNullExpression(Token token) {
        super(token.span);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return null;
    }
}
