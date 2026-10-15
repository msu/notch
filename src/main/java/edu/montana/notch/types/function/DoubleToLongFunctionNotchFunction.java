
package edu.montana.notch.types.function;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchFunction;

import java.util.List;
import java.util.function.DoubleToLongFunction;

public class DoubleToLongFunctionNotchFunction implements DoubleToLongFunction, NotchFunctionProxy {
    private final NotchRuntime runtime;
    private final Span callsite;
    private final NotchFunction function;

    public DoubleToLongFunctionNotchFunction(NotchRuntime runtime, Span callsite, NotchFunction function) {
        this.function = function;
        this.runtime = runtime;
        this.callsite = callsite;
    }

    @Override
    public NotchFunction getNotchFunction() {
        return function;
    }

    @Override
    public long applyAsLong(double value) {
        var result = function.invoke(long.class, runtime, callsite, List.of(value), List.of(callsite));
        return result;
    }
}
