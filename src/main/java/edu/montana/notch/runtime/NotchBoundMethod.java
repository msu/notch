package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.types.NotchFunction;
import edu.montana.notch.types.NotchMethod;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.NotchTypeSystem;

import java.util.List;

/**
 * A method paired with the value it was resolved off of. The runtime is supplied by the caller at
 * invocation time so builtins, which are bound once and reused, still see the active runtime.
 */
public record NotchBoundMethod(Object rootValue, NotchMethod method) implements NotchFunction {
    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public Object invoke(NotchRuntime runtime, Span callsite, List<Object> args, List<Span> argSpan) {
        return method.invoke(runtime, rootValue, callsite, args, argSpan);
    }

    @Override
    public boolean canInvokeWith(NotchRuntime runtime, List<NotchType> args) {
        return method.canInvokeWith(runtime, NotchTypeSystem.getRuntimeType(rootValue), args);
    }
}
