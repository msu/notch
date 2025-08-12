package bigsky.notch.expr;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.NotchToken;
import bigsky.notch.NotchUtils;

public class IdentNotchExpression extends NotchExpression {
    public final NotchToken token;

    public IdentNotchExpression(NotchToken token) {
        super(token.start, token.end);
        if (!token.type.equals("word")) {
            throw new AssertionError("expected variable name identifier");
        }
        NotchUtils.requireIdentifier(token.lex, "expected variable name");
        this.token = token;
    }

    public String name() {
        return token.lex;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var val = runtime.getSymbol(name());
        return val;
    }
}
