package edu.montana.notch.types.coercions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.NotchTypeCoercer;

public class VoidNullCoercion implements NotchTypeCoercer {
    public static final VoidNullCoercion INSTANCE = new VoidNullCoercion();

    private VoidNullCoercion() {}

    @Override
    public int getCoercionCost(NotchType source, NotchType target) {
        var targetClass = target.getBackingClass();
        var sourceClass = source.getBackingClass();
        if (targetClass != null && !targetClass.isPrimitive() && sourceClass == void.class) return CHEAP;
        return NO_COERCE;
    }

    @Override
    public Object coerce(NotchRuntime runtime, Object value, Span span, NotchType source, NotchType target) {
        return value;
    }
}
