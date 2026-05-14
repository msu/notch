package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.chisel.Token;

public class NotchString extends NotchExpression {
    public final Token token;

    public NotchString(String fileId, Token token) {
        super(fileId, token.start, token.end);
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
