package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.chisel.Token;

public class NullLiteral extends NotchExpression {
    public NullLiteral(String fileId, Token token) {
        super(fileId, token.start, token.end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return null;
    }
}
