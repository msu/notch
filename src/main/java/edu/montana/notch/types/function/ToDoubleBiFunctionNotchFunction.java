
package edu.montana.notch.types.function;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchFunction;

import java.util.List;
import java.util.function.ToDoubleBiFunction;

public class ToDoubleBiFunctionNotchFunction implements ToDoubleBiFunction, NotchFunctionProxy {
    private final NotchRuntime runtime;
    private final Span callsite;
    private final NotchFunction function;

    public ToDoubleBiFunctionNotchFunction(NotchRuntime runtime, Span callsite, NotchFunction function) {
        this.function = function;
        this.runtime = runtime;
        this.callsite = callsite;
    }

    @Override
    public NotchFunction getNotchFunction() {
        return function;
    }

    @Override
    public double applyAsDouble(Object o, Object o2) {
        var result = function.invoke(double.class, runtime, callsite, List.of(o, o2), List.of());
        return result;
    }
}
