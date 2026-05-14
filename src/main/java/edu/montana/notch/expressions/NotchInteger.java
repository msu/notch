package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.chisel.Token;

public class NotchInteger extends NotchExpression {
    public final Token token;

    public NotchInteger(String fileId, Token token) {
        super(fileId, token.start, token.end);
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
