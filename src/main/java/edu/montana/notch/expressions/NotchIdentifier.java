package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.runtime.UnknownVariableException;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;
import edu.montana.notch.util.Text;

import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;

public class NotchIdentifier extends NotchExpression implements DotPathMember {
    public final Token token;
    public final boolean throwIfUndefined;

    public NotchIdentifier(Token token, boolean throwIfUndefined, Token end) {
        super(token.span.through(end));
        this.token = token;
        this.throwIfUndefined = throwIfUndefined;
    }

    public NotchIdentifier(Token token) {
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
