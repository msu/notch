
package edu.montana.notch.types.function;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchFunction;

import java.util.List;
import java.util.function.ToIntBiFunction;

public class ToIntBiFunctionNotchFunction implements ToIntBiFunction, NotchFunctionProxy {
    private final NotchRuntime runtime;
    private final Span callsite;
    private final NotchFunction function;

    public ToIntBiFunctionNotchFunction(NotchRuntime runtime, Span callsite, NotchFunction function) {
        this.function = function;
        this.runtime = runtime;
        this.callsite = callsite;
    }

    @Override
    public NotchFunction getNotchFunction() {
        return function;
    }

    @Override
    public int applyAsInt(Object o, Object o2) {
        var result = function.invoke(int.class, runtime, callsite, List.of(o, o2), List.of(callsite, callsite));
        return result;
    }
}
