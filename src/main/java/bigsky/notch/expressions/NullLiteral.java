package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

public class NullLiteral extends NotchExpression {
    public NullLiteral(Token token) {
        super(token.start, token.end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return null;
    }
}
