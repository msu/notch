package edu.montana.notch.types.coercions;

import edu.montana.notch.types.NotchJavaType;
import edu.montana.notch.util.BetterList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Edges into {@link Iterable}, backing {@code for} loops and the iterable
 * operators.
 * <p>
 * Arrays arrive as {@code Object[]} because {@code NotchJavaType} reports array
 * covariance as a supertype hop, so {@code String[]} widens to {@code Object[]}
 * before this edge is taken.
 */
public final class CoerceIterable {

    private CoerceIterable() {
    }

    public static List<Coercion> edges() {
        var out = new BetterList<Coercion>();
        out.add(new Coercion(Object[].class, Iterable.class, value -> Arrays.asList((Object[]) value)));
        out.add(new Coercion(String.class, Iterable.class, value -> characters((String) value)));
        out.add(new Coercion(Class.class, Iterable.class, value -> enumConstants((Class<?>) value)));
        // a bare type in source position — `for x in SomeEnum` — is the class
        out.add(new Coercion(NotchJavaType.class, Iterable.class,
                value -> enumConstants(((NotchJavaType) value).getBackingClass())));
        return out;
    }

    private static List<String> characters(String value) {
        var out = new ArrayList<String>(value.length());
        for (char c : value.toCharArray()) {
            out.add(String.valueOf(c));
        }
        return out;
    }

    private static List<?> enumConstants(Class<?> clazz) {
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException("cannot iterate " + clazz.getName() + ", it is not an enum");
        }
        return Arrays.asList(clazz.getEnumConstants());
    }
}