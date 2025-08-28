package bigsky.notch.expr;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class IdentNotchExpression extends NotchExpression {
    public final Token token;

    public IdentNotchExpression(Token token) {
        super(token.start, token.end);
        this.token = token;
    }

    public String name() {
        return token.str();
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var val = runtime.getSymbol(name());
        return val;
    }
}
