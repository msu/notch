package edu.montana.notch.types.coercions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.NotchTypeCoercer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

public class IterableCoercions implements NotchTypeCoercer {
    public static final IterableCoercions INSTANCE = new IterableCoercions();

    private IterableCoercions() {
    }

    @Override
    public int getCoercionCost(NotchType from, NotchType to) {
        if (to.getBackingClass() != Iterable.class) return Integer.MAX_VALUE;
        final var fromClass = from.getBackingClass();
        if (fromClass == null) return Integer.MAX_VALUE;

        if (fromClass.isArray()) return BASE_COST;
        if (Map.class.isAssignableFrom(fromClass)) return BASE_COST;
        if (String.class.isAssignableFrom(fromClass)) return BASE_COST;
        if (fromClass.isEnum()) return BASE_COST;
        return Integer.MAX_VALUE;
    }

    @Override
    public Iterable coerce(NotchRuntime runtime, Object value, Span span, NotchType from, NotchType to) {
        if (to.getBackingClass() != Iterable.class) throw new CoercionFailure();
        if (value instanceof Map m) return m.entrySet();
        if (value instanceof String s) return () -> s.chars().mapToObj(i -> (char) i).iterator();
        if (value instanceof Class c && c.isEnum()) return () -> Arrays.stream(c.getEnumConstants()).iterator();
        if (value instanceof Object[] a) return Arrays.asList(a);
        if (value instanceof int[] a) return () -> Arrays.stream(a).boxed().iterator();
        if (value instanceof long[] a) return () -> Arrays.stream(a).boxed().iterator();
        if (value instanceof short[] a) return () -> IntStream.range(0, a.length).boxed().map(i -> a[i]).iterator();
        if (value instanceof byte[] a) return () -> IntStream.range(0, a.length).boxed().map(i -> a[i]).iterator();
        if (value instanceof char[] a) return () -> IntStream.range(0, a.length).boxed().map(i -> a[i]).iterator();
        if (value instanceof float[] a) return () -> IntStream.range(0, a.length).boxed().map(i -> a[i]).iterator();
        if (value instanceof double[] a) return () -> IntStream.range(0, a.length).boxed().map(i -> a[i]).iterator();
        if (value instanceof boolean[] a)  return () -> IntStream.range(0, a.length).boxed().map(i -> a[i]).iterator();
        throw new CoercionFailure();
    }
}
