package edu.montana.notch.types.coercions;

import edu.montana.notch.types.NotchType;
import edu.montana.notch.util.BetterList;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import static edu.montana.notch.types.TypeRegistry.getType;

/**
 * A single directed edge in the {@link CoercionGraph}: a way to turn a value of
 * type {@code from} into a value of type {@code to}, weighted by {@code rank}.
 * Cheaper edges are preferred when more than one chain reaches the target, so
 * the rank doubles as the overload-resolution penalty in
 * {@code NotchJavaMethod.distanceTo}.
 */
public record Coercion(NotchType from, NotchType to, int rank, Function<Object, Object> coerce) {

    /** A re-labelling that does not change the value — boxing and unboxing. */
    public static final int FREE = 0;
    /** Weight of a coercion that actually converts the value. */
    public static final int DEFAULT_RANK = 3;

    public Coercion {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(coerce, "coerce");
        // Dijkstra only settles a node once, which needs non-negative weights.
        if (rank < 0) {
            throw new IllegalArgumentException("coercion rank must be non-negative, got " + rank);
        }
    }

    public Coercion(NotchType from, NotchType to, Function<Object, Object> coerce) {
        this(from, to, DEFAULT_RANK, coerce);
    }

    public Coercion(Class<?> from, Class<?> to, Function<Object, Object> coerce) {
        this(getType(from), getType(to), DEFAULT_RANK, coerce);
    }

    public Coercion(Class<?> from, Class<?> to, int rank, Function<Object, Object> coerce) {
        this(getType(from), getType(to), rank, coerce);
    }

    public Object apply(Object value) {
        return coerce.apply(value);
    }

    /**
     * Every coercion Notch installs into a fresh runtime.
     */
    public static Collection<Coercion> defaults() {
        var out = new BetterList<Coercion>();
        out.addAll(BoxedToPrimitive.edges());
        out.addAll(NumberToBoxedPrimitive.edges());
        out.addAll(CoerceIterable.edges());
        return out;
    }

    @Override
    public String toString() {
        return from.getDisplayName() + " -> " + to.getDisplayName() + " (" + rank + ")";
    }
}