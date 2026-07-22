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
        if (value instanceof NotchClass declaredClass) {
            return declaredClass.construct(runtime, evaluateArgs(runtime));
        }
        if (value instanceof NotchType importedType) {
            Object[] argValues = evaluateArgs(runtime).toArray();
            try {
                return importedType.newInstance(argValues);
            } catch (IllegalArgumentException noMatchingConstructor) {
                throw noConstructor(runtime, importedType.getDisplayName());
            }
        }
        if (value == NotchRuntime.UNDEFINED) {
            List<Object> argValues = evaluateArgs(runtime);
            try {
                Object thrown = tryConstructThrowable(className.str(), argValues);
                if (thrown != null) return thrown;
            } catch (IllegalArgumentException noMatchingConstructor) {
                throw noConstructor(runtime, className.str());
            }
            throw unknownClass(runtime);
        }
        throw cannotInstantiateNonClassValue(runtime);
    }

    private NotchRuntimeException noConstructor(NotchRuntime runtime, String typeName) {
        var diag = new Diagnostic()
                .setTitle("no constructor for '" + typeName + "' accepts these argument types")
                .highlight(span());
        return new NotchRuntimeException(runtime.currentStackTrace(), diag);
    }

    private NotchRuntimeException unknownClass(NotchRuntime runtime) {
        var diag = new Diagnostic()
                .setTitle("no class named '" + className.str() + "'")
                .highlight(span());
        Class<?> resolvedType = TypeSystem.resolveThrowableType(className.str());
        if (resolvedType != null && !resolvedType.isPrimitive()) {
            diag.note("'%s' is a Java type, import it to use it: 'import %s'"
                    .formatted(className.str(), resolvedType.getName()));
        } else {
            diag.note("'" + className.str() + "' is not defined, make sure the class is declared before this line");
        }
        return new NotchRuntimeException(runtime.currentStackTrace(), diag);
    }

    private NotchRuntimeException cannotInstantiateNonClassValue(NotchRuntime runtime) {
        var diag = new Diagnostic()
                .setTitle("cannot instantiate a non-class value")
                .highlight(span())
                .note("'" + className.str() + "' is a value, not a class, so 'new' cannot be used on it");
        return new NotchRuntimeException(runtime.currentStackTrace(), diag);
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
