package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchString extends NotchExpression {
    public final Token token;

    public NotchString(Token token) {
        super(token.span);
        this.token = token;
    }

    public String value() {
        return token.str();
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return value();
    }
}
