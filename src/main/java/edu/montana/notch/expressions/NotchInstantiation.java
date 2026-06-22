package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchClass;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;

import java.util.ArrayList;
import java.util.List;

public class NotchInstantiation extends NotchExpression {
    private final Token className;
    private final List<NotchExpression> args;

    public NotchInstantiation(Span span, Token className, List<NotchExpression> args) {
        super(span);
        this.className = className;
        this.args = args;
        addChildren(args);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object value = runtime.getSymbol(className.str());
        if (value instanceof NotchClass clazz) {
            return clazz.construct(runtime, evaluateArgs(runtime));
        }
        if (value == NotchRuntime.UNDEFINED) {
            try {
                Object thrown = tryConstructThrowable(className.str(), evaluateArgs(runtime));
                if (thrown != null) return thrown;
            } catch (IllegalArgumentException e) {
                var diag = new Diagnostic();
                diag.setTitle("no constructor for '" + className.str() + "' accepts these argument types");
                diag.highlight(span());
                throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
            }
            var diag = new Diagnostic();
            diag.setTitle("no class named '" + className.str() + "'");
            diag.highlight(span());
            diag.note("'" + className.str() + "' is not defined - make sure the class is declared before this line");
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
        var diag = new Diagnostic();
        diag.setTitle("cannot instantiate a non-class value");
        diag.highlight(span());
        diag.note("'" + className.str() + "' is a value, not a class, so 'new' cannot be used on it");
        throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
    }

    private List<Object> evaluateArgs(NotchRuntime runtime) {
        List<Object> argValues = new ArrayList<>(args.size());
        for (NotchExpression arg : args) {
            argValues.add(runtime.evaluate(arg));
        }
        return argValues;
    }

    public static Object tryConstructThrowable(String name, List<Object> argValues) {
        Class<?> type = TypeSystem.resolveThrowableType(name);
        if (type == null || !Throwable.class.isAssignableFrom(type)) return null;
        NotchType notchType = TypeSystem.getType(type);
        Object[] args = argValues.toArray();
        return notchType.newInstance(args);
    }
}
