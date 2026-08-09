package edu.montana.notch.types.coercions;

import edu.montana.notch.types.NotchType;

import java.util.List;

/**
 * The cheapest chain of {@link Coercion}s the {@link CoercionGraph} found from
 * {@code from} to {@code to}. An empty {@code steps} list means the target was
 * reached by widening alone, so the value passes through untouched.
 *
 * @param cost total weight of the walk, including the hierarchy hops used to
 *             widen the source type. Also the overload-resolution distance.
 */
public record CoercionPath(NotchType from, NotchType to, int cost, List<Coercion> steps) {

    public CoercionPath {
        steps = List.copyOf(steps);
    }

    public Object apply(Object value) {
        for (Coercion step : steps) {
            value = step.apply(value);
        }
        return value;
    }

    /** True when the value already satisfies the target and needs no conversion. */
    public boolean isIdentity() {
        return steps.isEmpty();
    }

    @Override
    public String toString() {
        var sb = new StringBuilder(from.getDisplayName());
        for (Coercion step : steps) {
            sb.append(" -> ").append(step.to().getDisplayName());
        }
        if (steps.isEmpty()) {
            sb.append(" -> ").append(to.getDisplayName());
        }
        return sb.append(" (").append(cost).append(')').toString();
    }
}