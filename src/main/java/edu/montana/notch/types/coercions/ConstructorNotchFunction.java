package edu.montana.notch.types.coercions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchFunction;
import edu.montana.notch.types.NotchType;

import java.util.List;

public class ConstructorNotchFunction implements NotchFunction {
    public final NotchType type;

    public ConstructorNotchFunction(NotchType type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return type.getDisplayName() + "::new";
    }

    @Override
    public Object invoke(NotchRuntime runtime, Span callsite, List<Object> args, List<Span> argSpan) {
        return type.newInstance(runtime, callsite, args, argSpan);
    }

    @Override
    public boolean canInvokeWith(NotchRuntime runtime, List<NotchType> args) {
        return true;
    }
}
