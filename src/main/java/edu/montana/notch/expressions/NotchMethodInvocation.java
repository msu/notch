package edu.montana.notch.expressions;

import bigsky.notch.runtime.*;
import edu.montana.notch.runtime.*;
import edu.montana.notch.types.NotchJavaMethod;
import edu.montana.notch.util.Text;
import edu.montana.notch.chisel.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import static edu.montana.notch.util.Exceptions.safelyEval;

public class NotchMethodInvocation extends NotchExpression {

    public final NotchExpression root;
    public final List<NotchExpression> args;

    public NotchMethodInvocation(Location start, NotchExpression root, List<NotchExpression> args, Location end) {
        super(root.fileId, start, end);
        this.root = Objects.requireNonNull(root);
        this.args = List.copyOf(Objects.requireNonNull(args));
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object functionObj = runtime.evaluate(root);
        if (functionObj == null || runtime.isUndefined(functionObj)) {
            var diag = new NotchDiagnostic();
            diag.highlight(fileId, span());
            if (root instanceof NotchPropertyAccess pa) {
                var prop = pa.getProperty();
                var parentPath = pa.getParentDotPath();
                diag.note("unable to call %s, %s was null".formatted(Text.repr(prop), Text.repr(parentPath)));
            } else {
                diag.note("unable to call null");
            }
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }

        var argValues = new ArrayList<>(args.size());
        for (NotchExpression arg : args) {
            argValues.add(runtime.evaluate(arg));
        }

        if (functionObj instanceof NotchBoundMethod bm) {
            try (var ignoredTrace = runtime.trace(fileId, span(), bm.method().getQualifiedName())) {
                return bm.invoke(argValues);
            }
        } else if (functionObj instanceof NotchClosure nc) {
            try (var ignoredTrace = runtime.trace(fileId, span(), nc.getQualifiedName())) {
                return nc.call(argValues);
            }
        } else if (functionObj instanceof NotchJavaMethod jm) {
            try (var ignoredTrace = runtime.trace(fileId, span(), jm.getQualifiedName())) {
                return jm.invoke(null, argValues);
            }
        } else if (functionObj instanceof Callable<?> c) {
            try (var ignoreTrace = runtime.trace(fileId, span(), "<callable:%s>".formatted(NotchRuntime.className(c)))) {
                return safelyEval(c);
            }
        } else if (functionObj instanceof Runnable r) {
            try (var ignoreTrace = runtime.trace(fileId, span(), "<internal:%s>".formatted(NotchRuntime.className(r)))) {
                return run(r);
            }
        } else {
            var diag = new NotchDiagnostic();
            diag.setTitle("failed to invoke unknown value");
            diag.highlight(root.fileId, root.span());
            diag.note("the value had type " + NotchRuntime.className(functionObj));
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
    }

    private static Object run(Runnable r) {
        r.run();
        return null;
    }
}
