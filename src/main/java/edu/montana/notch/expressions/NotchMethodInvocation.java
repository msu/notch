package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Location;
import edu.montana.notch.runtime.*;
import edu.montana.notch.types.NotchJavaMethod;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import static edu.montana.notch.util.Exceptions.safelyEval;

public class NotchMethodInvocation extends NotchExpression {

    public final NotchExpression root;
    public final List<NotchExpression> args;

    public NotchMethodInvocation(NotchExpression root, List<NotchExpression> args, Location end) {
        super(root.span().through(end));
        this.root = Objects.requireNonNull(root);
        this.args = List.copyOf(Objects.requireNonNull(args));
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object functionObj = null;
        try {
            functionObj = runtime.evaluate(root);
        } catch (UnknownVariableException ignored) {}
        if (functionObj == null || runtime.isUndefined(functionObj)) {
            var diag = new Diagnostic();
            diag.highlight(root.span());
            if (root instanceof NotchPropertyAccess pa) {
                diag.note("unable to call '%s', value was null".formatted(pa.getDotPath()));
            } else if (root instanceof NotchIdentifier ni) {
                diag.note("undefined function '%s'".formatted(ni.name()));
                addJavaTypeHints(diag, ni.name());
            } else {
                diag.note("unable to call expression, value was null or undefined");
            }
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }

        var argValues = new ArrayList<>(args.size());
        for (NotchExpression arg : args) {
            argValues.add(runtime.evaluate(arg));
        }

        if (functionObj instanceof NotchBoundMethod bm) {
            try (var ignoredTrace = runtime.trace(span(), bm.method().getQualifiedName())) {
                return bm.invoke(argValues);
            }
        } else if (functionObj instanceof NotchClosure nc) {
            try (var ignoredTrace = runtime.trace(span(), nc.getQualifiedName())) {
                return nc.call(argValues);
            }
        } else if (functionObj instanceof NotchJavaMethod jm) {
            try (var ignoredTrace = runtime.trace(span(), jm.getQualifiedName())) {
                return jm.invoke(runtime, null, argValues);
            }
        } else if (functionObj instanceof NotchType type) {
            try (var ignoredTrace = runtime.trace(span(), "<constructor:" + type.getSimpleName() + ">")) {
                return type.newInstance(argValues.toArray());
            } catch (IllegalArgumentException e) {
                var diag = new Diagnostic();
                diag.setTitle("no constructor for '" + type.getDisplayName() + "' accepts these argument types");
                diag.highlight(root.span());
                throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
            }
        } else if (functionObj instanceof Callable<?> c) {
            try (var ignoreTrace = runtime.trace(span(), "<callable:%s>".formatted(NotchRuntime.className(c)))) {
                return safelyEval(c);
            }
        } else if (functionObj instanceof Runnable r) {
            try (var ignoreTrace = runtime.trace(span(), "<internal:%s>".formatted(NotchRuntime.className(r)))) {
                return run(r);
            }
        } else {
            var diag = new Diagnostic();
            diag.setTitle("failed to invoke unknown value");
            diag.highlight(root.span());
            diag.note("the value had type " + NotchRuntime.className(functionObj));
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
    }

    private static void addJavaTypeHints(Diagnostic diag, String name) {
        Class<?> resolvedType = TypeSystem.resolveThrowableType(name);
        if (resolvedType == null) return;
        diag.note("'%s' is a Java type, import it to use it as a value: 'import %s'"
                .formatted(name, resolvedType.getName()));
        if (Throwable.class.isAssignableFrom(resolvedType)) {
            diag.note("or construct it directly: 'new %s(...)'".formatted(name));
        }
    }

    private static Object run(Runnable r) {
        r.run();
        return null;
    }
}
