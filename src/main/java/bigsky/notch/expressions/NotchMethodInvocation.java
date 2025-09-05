package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchBoundMethod;
import bigsky.notch.runtime.NotchClosure;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.notch.types.NotchJavaMethod;
import bigsky.utils.chisel.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static bigsky.utils.Exceptions.safelyEval;

public class NotchMethodInvocation extends NotchExpression {

    private NotchExpression root;
    private List<NotchExpression> args;

    public NotchMethodInvocation(Location start, Location end) {
        super(start, end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object functionObj = root.evaluate(runtime);
        if (functionObj == null) {
            NotchExpression actualRoot = root;
            if (actualRoot instanceof NotchPropertyAccess pa) {
                actualRoot = pa.getRoot();
            }
            //TODO improve error message
            throw new NullPointerException("The root expression " + actualRoot + " returned null");
        } else {
            var argValues = new ArrayList<>(args.size());
            for (NotchExpression arg : args) {
                argValues.add(arg.evaluate(runtime));
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
                throw new NotchRuntimeException(span(), "The expression " + root + " returned a " + functionObj.getClass().getSimpleName() + ", which I don't know how to invoke!");
            }
        }
    }

    private static Object run(Runnable r) {
        r.run();
        return null;
    }

    public void setRoot(NotchExpression root) {
        this.root = addChild(root);
    }

    public void setArgs(List<NotchExpression> args) {
        this.args = addChildren(args);
    }
}
