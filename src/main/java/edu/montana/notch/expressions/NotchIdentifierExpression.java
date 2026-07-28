package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.UnknownVariableException;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;

import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;

public class NotchIdentifierExpression extends NotchExpression implements DotPathMember {
    public final Token token;
    public final boolean throwIfUndefined;

    public NotchIdentifierExpression(Token token, boolean throwIfUndefined, Token end) {
        super(token.span.through(end));
        this.token = token;
        this.throwIfUndefined = throwIfUndefined;
    }

    public NotchIdentifierExpression(Token token) {
        this(token, true, token);
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

            if (throwIfUndefined) {
                throw new UnknownVariableException(token, runtime.currentStackTrace());
            }
        }
        return val;
    }

    @Override
    public String getDotPath() {
        if (this.token.str().equals("this")) {
            return null;
        }
        return this.token.str();
    }
}
