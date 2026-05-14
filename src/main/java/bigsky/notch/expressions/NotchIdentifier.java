package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchDiagnostic;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.notch.types.NotchType;
import bigsky.notch.types.TypeSystem;
import bigsky.notch.util.Text;
import bigsky.notch.chisel.Token;

import static bigsky.notch.runtime.NotchRuntime.UNDEFINED;

public class NotchIdentifier extends NotchExpression implements DotPathMember {
    public final Token token;

    public NotchIdentifier(String fileId, Token token) {
        super(fileId, token.start, token.end);
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
            // UNDEFINED propagates as a value: callers that need a defined
            // value (arithmetic, method invocation, etc.) will throw when they
            // try to coerce it; callers that can handle absence (?:, print,
            // property access, template interpolation) do the right thing.
            return UNDEFINED;
        }
        return val;
    }

    @Override
    public String getDotPath() {
        return this.token.str();
    }
}
