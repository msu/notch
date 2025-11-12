package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class NotchBoolean extends NotchExpression {
    public final Token token;

    public NotchBoolean(String fileId, Token token) {
        super(fileId, token.start, token.end);
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
