package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;

public interface NotchFunction {
    String getName();
    Object invoke(NotchRuntime runtime, Span callsite, List<Object> args, List<Span> argSpan);

    default <T> T invoke(Class<T> clazz, NotchRuntime runtime, Span callsite, List<Object> args, List<Span> argSpan) {
        Object result = invoke(runtime, callsite, args, argSpan);
        T out = runtime.coerce(clazz, result, callsite);
        return out;
    }

    boolean canInvokeWith(NotchRuntime runtime, List<NotchType> args);
    default List<NotchFunctionDescription> getVariants() {
        // by default functions do not have to declare their args & return types
        return List.of();
    }
}
