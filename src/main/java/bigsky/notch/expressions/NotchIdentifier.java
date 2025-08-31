package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Token;

import java.beans.Expression;

public class NotchIdentifier extends NotchExpression implements DotPathMember {
    public final Token token;

    public NotchIdentifier(Token token) {
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

    @Override
    public String getDotPath() {
        return this.token.str();
    }
}
