package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchBoolean extends NotchExpression {
    public final Token token;

    public NotchBoolean(Token token) {
        super(token.span);
        this.token = token;
    }

    private Boolean value;

    public boolean value() {
        if (value == null) value = token.bool();
        return value;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return value();
    }
}
