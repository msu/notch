package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.types.NotchType;
import bigsky.notch.types.TypeSystem;
import bigsky.utils.chisel.Token;

import java.beans.Expression;

import static bigsky.notch.runtime.NotchRuntime.UNDEFINED;

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
        if (val == UNDEFINED) {
            NotchType type = TypeSystem.getType(token.str());
            if(type != null) {
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
