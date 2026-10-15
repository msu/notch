
package edu.montana.notch.types.function;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchFunction;

import java.util.List;
import java.util.function.IntUnaryOperator;

public class IntUnaryOperatorNotchFunction implements IntUnaryOperator, NotchFunctionProxy {
    private final NotchRuntime runtime;
    private final Span callsite;
    private final NotchFunction function;

    public IntUnaryOperatorNotchFunction(NotchRuntime runtime, Span callsite, NotchFunction function) {
        this.function = function;
        this.runtime = runtime;
        this.callsite = callsite;
    }

    @Override
    public NotchFunction getNotchFunction() {
        return function;
    }

    @Override
    public int applyAsInt(int operand) {
        var result = function.invoke(int.class, runtime, callsite, List.of(operand), List.of(callsite));
        return result;
    }
}
