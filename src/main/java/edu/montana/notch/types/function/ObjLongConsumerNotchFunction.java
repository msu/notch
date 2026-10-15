
package edu.montana.notch.types.function;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchFunction;

import java.util.List;
import java.util.function.ObjLongConsumer;

public class ObjLongConsumerNotchFunction implements ObjLongConsumer, NotchFunctionProxy {
    private final NotchRuntime runtime;
    private final Span callsite;
    private final NotchFunction function;

    public ObjLongConsumerNotchFunction(NotchRuntime runtime, Span callsite, NotchFunction function) {
        this.function = function;
        this.runtime = runtime;
        this.callsite = callsite;
    }

    @Override
    public NotchFunction getNotchFunction() {
        return function;
    }

    @Override
    public void accept(Object o, long value) {
        function.invoke(runtime, callsite, List.of(o, value), List.of(callsite, callsite));
    }
}
