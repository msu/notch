package bigsky.notch.expressions;

import bigsky.notch.runtime.*;
import bigsky.notch.types.NotchJavaMethod;
import bigsky.utils.chisel.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import static bigsky.utils.Exceptions.safelyEval;

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
        if (runtime.isUndefined(functionObj)) {
            NotchExpression actualRoot = root;
            if (actualRoot instanceof NotchPropertyAccess pa) {
                actualRoot = pa.getRoot();
            }

            var diag = new NotchDiagnostic();
            diag.setTitle("invocation target was null");
            diag.highlight(actualRoot.fileId, actualRoot.span());
            diag.note("this expression was nil");
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        } else {
            var argValues = new ArrayList<>(args.size());
            for (NotchExpression arg : args) {
                argValues.add(runtime.evaluate(arg));
            }
            if (functionObj instanceof NotchBoundMethod bm) {
                return bm.invoke(argValues);
            } else if (functionObj instanceof NotchClosure nc) {
                return nc.call(argValues);
            } else if (functionObj instanceof NotchJavaMethod jm) {
                return jm.invoke(null, argValues);
            } else if (functionObj instanceof Callable<?> c) {
                return safelyEval(c);
            } else if (functionObj instanceof Runnable r) {
                return run(r);
                // TODO better error message
            } else {
                var diag = new NotchDiagnostic();
                diag.setTitle("failed to invoke unknown value");
                diag.highlight(root.fileId, root.span());
                diag.note("the value had type " + functionObj.getClass().getName());
                throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
            }
        }
    }

    private static Object run(Runnable r) {
        r.run();
        return null;
    }
}
