package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.chisel.Token;

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
