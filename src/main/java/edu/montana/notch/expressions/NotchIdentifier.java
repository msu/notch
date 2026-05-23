package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;

import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;

public class NotchIdentifier extends NotchExpression implements DotPathMember {
    public final Token token;

    public NotchIdentifier(Token token) {
        super(token.span);
        this.token = token;
    }

    public String name() {
        return token.str();
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var name = name();
        var val = runtime.getSymbol(name);
        if (val == UNDEFINED) {
            NotchType type = TypeSystem.getType(name);
            if (type != null) {
                return type;
            }
        }
        return val;
    }

    @Override
    public String getDotPath() {
        return this.token.str();
    }
}
