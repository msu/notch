package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.*;
import edu.montana.notch.types.NotchFunction;
import edu.montana.notch.types.NotchTypeSystem;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.Exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotchMethodInvocationExpression extends NotchExpression {

    public final NotchExpression root;
    public final BetterList<NotchExpression> args;

    public NotchMethodInvocationExpression(NotchExpression root, List<NotchExpression> args, Location end) {
        super(root.span().through(end));
        this.root = Objects.requireNonNull(root);
        this.args = BetterList.better(Objects.requireNonNull(args));
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
            if (root instanceof NotchPropertyAccessExpression pa) {
                diag.note("unable to call '%s', value was null".formatted(pa.getDotPath()));
            } else if (root instanceof NotchIdentifierExpression ni) {
                diag.note("undefined function '%s'".formatted(ni.name()));
                addJavaTypeHints(diag, ni.name());
            } else {
                diag.note("unable to call expression, value was null or undefined");
            }
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }

        NotchFunction invocable = runtime.coerce(NotchFunction.class, functionObj, root.span);

        var argValues = new ArrayList<>(args.size());
        var argSpan = new ArrayList<Span>(args.size());
        for (NotchExpression arg : args) {
            argValues.add(runtime.evaluate(arg));
            argSpan.add(arg.span);
        }

        var frame = new NotchStackTraceElement.Notch(span, "TODO: current-function-name");
        try (var _guard = runtime.addStackFrame(frame)) {
            Object value = invocable.invoke(runtime, span, argValues, argSpan);
            return value;
        }
    }

    private static void addJavaTypeHints(Diagnostic diag, String name) {
        Class<?> resolvedType = NotchTypeSystem.resolveThrowableType(name);
        if (resolvedType == null) return;
        diag.note("'%s' is a Java type, import it to use it as a value: 'import %s'"
                .formatted(name, resolvedType.getName()));
        if (Throwable.class.isAssignableFrom(resolvedType)) {
            diag.note("or construct it directly: 'new %s(...)'".formatted(name));
        }
    }
}
