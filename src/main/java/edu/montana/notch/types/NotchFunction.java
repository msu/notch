package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;

public interface NotchFunction {
    String getName();
    Object invoke(NotchRuntime runtime, Object rootVal, List<Object> args, List<Span> argSpan);

    boolean canInvokeWith(NotchRuntime runtime, List<Object> args);


    default List<NotchFunctionDescription> getVariants() {
        // by default functions do not have to declare their args & return types
        return List.of();
    }
}
