package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

public interface NotchTypeCoercer {
    int NO_COERCE = Integer.MAX_VALUE;
    int CHEAP = 3;
    int EXPENSIVE = 6;

    /// return 1 for cheap,
    /// - 2+ for expensive,
    /// - MAX_VALUE for impossible,
    /// - 0 is reserved for primitives,
    /// - don't return < 0
    int getCoercionCost(NotchType source, NotchType target);
    Object coerce(NotchRuntime runtime, Object value, Span span, NotchType source, NotchType target);

    int BASE_COST = 3;
    class CoercionFailure extends RuntimeException {}
}
