package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

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
