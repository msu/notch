package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchBoundMethod;
import bigsky.notch.runtime.NotchClosure;
import bigsky.notch.runtime.NotchRuntime;
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
        if (functionObj == null || functionObj == NotchRuntime.UNDEFINED) {
            NotchExpression actualRoot = root;
            if (actualRoot instanceof NotchPropertyAccess pa) {
                actualRoot = pa.getRoot();
            }
            //TODO improve error message
            throw new NullPointerException("The root expression " + actualRoot + " returned null");
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
                throw runtime.raise(span(), "The expression " + root + " returned a " + functionObj.getClass().getName() + ", which I don't know how to invoke!");
            }
        }
    }

    private static Object run(Runnable r) {
        r.run();
        return null;
    }
}
