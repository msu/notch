package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchInteger extends NotchExpression {
    public final Token token;

    public NotchInteger(Token token) {
        super(token.span);
        this.token = token;
    }

    private Integer value;

    public int value() {
        if (value == null) {
            value = token.integer();
        }
        return value;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return value();
    }
}
