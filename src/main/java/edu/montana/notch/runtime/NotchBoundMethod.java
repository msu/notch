package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.types.NotchMethod;

import java.util.List;

/**
 * A method paired with the value it was resolved off of. The runtime is supplied by the caller at
 * invocation time so builtins, which are bound once and reused, still see the active runtime.
 */
public record NotchBoundMethod(Object rootValue, NotchMethod method) {
    public Object invoke(NotchRuntime runtime, List<Object> argValues, List<Span> argSpan) {
        return method.invoke(runtime, rootValue, argValues, argSpan);
    }
}
