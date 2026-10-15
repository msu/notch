
package edu.montana.notch.types.function;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchFunction;

import java.util.List;
import java.util.function.DoubleBinaryOperator;

public class DoubleBinaryOperatorNotchFunction implements DoubleBinaryOperator, NotchFunctionProxy {
    private final NotchRuntime runtime;
    private final Span callsite;
    private final NotchFunction function;

    public DoubleBinaryOperatorNotchFunction(NotchRuntime runtime, Span callsite, NotchFunction function) {
        this.function = function;
        this.runtime = runtime;
        this.callsite = callsite;
    }

    @Override
    public NotchFunction getNotchFunction() {
        return function;
    }

    @Override
    public double applyAsDouble(double left, double right) {
        var result = function.invoke(double.class, runtime, callsite, List.of(left, right), List.of(callsite, callsite));
        return result;
    }
}
