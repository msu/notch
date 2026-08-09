package edu.montana.notch.types.coercions;

import edu.montana.notch.util.BetterList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Numeric conversions.
 * <p>
 * These hang off {@link Number} rather than off each concrete numeric class:
 * the graph widens the source type before it walks, so one {@code Number ->
 * Integer} edge covers Long, Double, BigDecimal and any user {@code Number}
 * subclass. Primitive targets are reached by chaining the free unboxing edge
 * from {@link BoxedToPrimitive}.
 */
public final class NumberToBoxedPrimitive {

    private NumberToBoxedPrimitive() {
    }

    public static List<Coercion> edges() {
        var out = new BetterList<Coercion>();
        out.add(new Coercion(Number.class, Byte.class, value -> ((Number) value).byteValue()));
        out.add(new Coercion(Number.class, Short.class, value -> ((Number) value).shortValue()));
        out.add(new Coercion(Number.class, Integer.class, value -> ((Number) value).intValue()));
        out.add(new Coercion(Number.class, Long.class, value -> ((Number) value).longValue()));
        out.add(new Coercion(Number.class, Float.class, value -> ((Number) value).floatValue()));
        out.add(new Coercion(Number.class, Double.class, value -> ((Number) value).doubleValue()));
        out.add(new Coercion(Number.class, BigInteger.class, value -> new BigInteger(value.toString())));
        out.add(new Coercion(Number.class, BigDecimal.class, value -> new BigDecimal(value.toString())));
        out.add(new Coercion(Number.class, Character.class, value -> (char) ((Number) value).intValue()));
        out.add(new Coercion(Character.class, Integer.class, value -> (int) (Character) value));
        return out;
    }
}