package edu.montana.notch.types.coercions;

import edu.montana.notch.util.BetterList;

import java.util.List;
import java.util.function.Function;

import static edu.montana.notch.types.coercions.Coercion.FREE;

/**
 * Boxing edges. Reflection boxes and unboxes on its own, so these carry no
 * value change and cost nothing — they exist so a {@code long} parameter is a
 * zero-distance match for a {@code Long} argument instead of falling through to
 * the {@link NumberToBoxedPrimitive} conversions.
 */
public final class BoxedToPrimitive {

    private static final Class<?>[][] PAIRS = {
            {Boolean.class, Boolean.TYPE},
            {Byte.class, Byte.TYPE},
            {Character.class, Character.TYPE},
            {Short.class, Short.TYPE},
            {Integer.class, Integer.TYPE},
            {Long.class, Long.TYPE},
            {Float.class, Float.TYPE},
            {Double.class, Double.TYPE},
    };

    private BoxedToPrimitive() {
    }

    public static List<Coercion> edges() {
        var out = new BetterList<Coercion>();
        for (Class<?>[] pair : PAIRS) {
            Class<?> boxed = pair[0];
            Class<?> primitive = pair[1];
            out.add(new Coercion(boxed, primitive, FREE, Function.identity()));
            out.add(new Coercion(primitive, boxed, FREE, Function.identity()));
        }
        return out;
    }
}