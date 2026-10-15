package edu.montana.notch.expressions;

import edu.montana.notch.NotchElement;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchClass;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.NotchTypeSystem;
import edu.montana.notch.util.BetterList;

import java.util.ArrayList;
import java.util.List;

public class NotchInstantiationExpression extends NotchExpression {
    private final Token className;
    private final BetterList<NotchExpression> args;

    public NotchInstantiationExpression(Span span, Token className, List<NotchExpression> args) {
        super(span);
        this.className = className;
        this.args = new BetterList<>(args);
        addChildren(args);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object value = runtime.getSymbol(className.str());
        // TODO: this should be an instance of NotchType, not a special case
        if (value instanceof NotchClass declaredClass) {
            return declaredClass.construct(runtime, evaluateArgs(runtime));
        }

        NotchType consType = null;
        if (value instanceof NotchType importedType) {
            consType = importedType;
        } else if (runtime.isUndefined(value)) {
            Class<?> type = NotchTypeSystem.resolveThrowableType(className.str());
            if (type != null && Throwable.class.isAssignableFrom(type)) {
                consType = NotchTypeSystem.getType(type);
            }
        }

        if (consType == null) {
            throw cannotInstantiateNonClassValue(runtime);
        }

        var argValues = runtime.evaluate(args);
        var argSpan = args.map(NotchElement::span);
        try {
            return consType.newInstance(runtime, className.span, argValues, argSpan);
        } catch (IllegalArgumentException noMatchingConstructor) {
            throw noConstructor(runtime, consType.getDisplayName(), noMatchingConstructor);
        }
    }

    private NotchRuntimeException noConstructor(NotchRuntime runtime, String typeName, IllegalArgumentException noMatchingConstructor) {
        var diag = new Diagnostic()
                .setTitle("no constructor for '" + typeName + "' accepts these argument types")
                .highlight(span());
        return new NotchRuntimeException(runtime.currentStackTrace(), diag, noMatchingConstructor);
    }

    private NotchRuntimeException unknownClass(NotchRuntime runtime) {
        var diag = new Diagnostic()
                .setTitle("no class named '" + className.str() + "'")
                .highlight(span());
        Class<?> resolvedType = NotchTypeSystem.resolveThrowableType(className.str());
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

    public static Object tryConstructThrowable(NotchRuntime runtime, String name, Span callsite, List<Object> argValues, List<Span> argSpan) {
        Class<?> type = NotchTypeSystem.resolveThrowableType(name);
        if (type == null || !Throwable.class.isAssignableFrom(type)) return null;
        NotchType notchType = NotchTypeSystem.getType(type);
        return notchType.newInstance(runtime, callsite, argValues, argSpan);
    }
}
