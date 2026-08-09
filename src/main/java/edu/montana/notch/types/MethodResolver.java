package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.coercions.CoercionPath;
import edu.montana.notch.util.BetterList;

import java.lang.reflect.Executable;
import java.util.List;
import java.util.function.ToIntFunction;

import static java.lang.Integer.MAX_VALUE;

/**
 * Overload resolution shared by {@link NotchJavaMethod}, {@link NotchBuiltinMethod} and the
 * constructor lookup in {@link NotchJavaType}.
 * <p>
 * Callers that supply some leading parameters implicitly (builtins receive the {@link NotchRuntime}
 * that way) pass a {@code skipParams} count so those parameters take no part in matching.
 */
public final class MethodResolver {

    private MethodResolver() {
    }

    public static <T extends Executable> T getBestMatch(NotchRuntime runtime, List<Object> argValues, List<T> executables) {
        return getBestMatch(runtime, argValues, executables, (e) -> 0);
    }

    public static <T extends Executable> T getBestMatch(NotchRuntime runtime, List<Object> argValues, List<T> executables,
                                                        ToIntFunction<? super T> skipParams) {
        int closestDistance = MAX_VALUE;
        T bestMatch = null;
        int bestSkip = 0;
        for (T executable : executables) {
            int skip = skipParams.applyAsInt(executable);
            int distance = distanceFromValues(runtime, executable, argValues, skip);
            if (distance == MAX_VALUE) {
                continue;
            } else if (distance < closestDistance) {
                closestDistance = distance;
                bestMatch = executable;
                bestSkip = skip;
            } else if (distance == closestDistance) {
                // stable resolution by using string comparison
                String str1 = signature(bestMatch, bestSkip);
                String str2 = signature(executable, skip);
                if (str1.compareTo(str2) < 0) {
                    bestMatch = executable;
                    bestSkip = skip;
                }
            }
        }
        return bestMatch;
    }

    public static int distanceFromValues(NotchRuntime runtime, Executable executable, List<Object> runtimeValues) {
        return distanceFromValues(runtime, executable, runtimeValues, 0);
    }

    public static int distanceFromValues(NotchRuntime runtime, Executable executable, List<Object> runtimeValues, int skipParams) {
        Class<?>[] parameterTypes = executable.getParameterTypes();
        if (runtimeValues.size() != parameterTypes.length - skipParams) {
            return MAX_VALUE;
        }
        int distance = 0;
        for (int i = 0; i < runtimeValues.size(); i++) {
            Class<?> parameterType = parameterTypes[i + skipParams];
            Object o = runtimeValues.get(i);
            if (o != null) {
                int paramDistance = distanceTo(runtime, o.getClass(), parameterType);
                if (paramDistance == MAX_VALUE) {
                    return MAX_VALUE;
                }
                distance += paramDistance;
            }
        }
        return distance;
    }

    /**
     * Coerces {@code args} in place so every value is assignable to the matching parameter of
     * {@code executable}, ignoring the first {@code skipParams} parameters.
     */
    public static void coerceArguments(NotchRuntime runtime, Executable executable, List<Object> args, List<Span> argSpans, int skipParams) {
        Class<?>[] parameterTypes = executable.getParameterTypes();
        for (int i = 0; i < args.size(); i++) {
            Object arg = args.get(i);
            if (arg != null) {
                Class<?> parameterType = parameterTypes[i + skipParams];
                Class<?> runtimeType = arg.getClass();
                if (!parameterType.isAssignableFrom(runtimeType)) {
                    var value = runtime.coerce(parameterType, arg, argSpans.get(i));
                    args.set(i, value);
                }
            }
        }
    }

    public static int distanceTo(NotchRuntime runtime, Class<?> from, Class<?> to) {
        if (from == null) {
            return MAX_VALUE;
        }
        if (from.equals(to)) {
            return 0;
        }
        // the coercion graph walks the supertypes of `from` itself, so this
        // covers plain assignability as well as any conversion chain
        CoercionPath path = runtime.getCoercion(from, to);
        return path == null ? MAX_VALUE : path.cost();
    }

    private static String signature(Executable executable, int skipParams) {
        return new BetterList<>(executable.getParameterTypes()).skip(skipParams).map(Class::getName).toString("-");
    }
}