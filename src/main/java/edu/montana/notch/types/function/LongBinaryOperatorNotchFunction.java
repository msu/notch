
package edu.montana.notch.types.function;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchFunction;

import java.util.List;
import java.util.function.LongBinaryOperator;

public class LongBinaryOperatorNotchFunction implements LongBinaryOperator, NotchFunctionProxy {
    private final NotchRuntime runtime;
    private final Span callsite;
    private final NotchFunction function;

    public LongBinaryOperatorNotchFunction(NotchRuntime runtime, Span callsite, NotchFunction function) {
        this.function = function;
        this.runtime = runtime;
        this.callsite = callsite;
    }

    @Override
    public NotchFunction getNotchFunction() {
        return function;
    }

    @Override
    public long applyAsLong(long left, long right) {
        var result = function.invoke(long.class, runtime, callsite, List.of(left, right), List.of(callsite, callsite));
        return result;
    }
}
