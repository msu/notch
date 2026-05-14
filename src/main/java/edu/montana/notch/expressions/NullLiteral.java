package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.chisel.Token;

public class NullLiteral extends NotchExpression {
    public NullLiteral(String fileId, Token token) {
        super(fileId, token.start, token.end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        return null;
    }
}
