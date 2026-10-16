package edu.montana.notch.types;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.runtime.NotchStackTraceElement;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.Exceptions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;

import static edu.montana.notch.util.Exceptions.rethrow;
import static edu.montana.notch.util.Exceptions.safely;
import static java.lang.Integer.MAX_VALUE;

/**
 * Overload resolution shared by {@link NotchJavaMethod}, {@link NotchBuiltinFunction} and the
 * constructor lookup in {@link NotchJavaType}.
 * <p>
 * Callers that supply some leading parameters implicitly (builtins receive the {@link NotchRuntime}
 * that way) pass a {@code skipParams} count so those parameters take no part in matching.
 */
public final class NotchJavaMethodUtils {

    private NotchJavaMethodUtils() {
    }

    public record BestMatch<T extends Executable>(NotchRuntime runtime, T executable, List<NotchType> argTypes,
                                                  List<NotchType> paramTypes, List<NotchTypeCoercer> coercers,
                                                  boolean hasImplicitRuntime) {
        public Object[] coerceArgValues(List<Object> args, List<Span> argSpan) {
            Object[] values = new Object[executable.getParameterCount()];
            int skip = 0;
            if (hasImplicitRuntime) {
                skip = 1;
                values[0] = runtime;
            }
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                var coercion = coercers.get(i);
                Object value = coercion.coerce(runtime, arg, argSpan.get(i), argTypes.get(i), paramTypes.get(i));
                values[skip + i] = value;
            }
            return values;
        }
    }

    /**
     * Returns a version of {@code method} that can be invoked reflectively, or {@code null} if there is none.
     * <p>
     * Public methods on non-public classes (e.g. {@code ImmutableCollections$MapN.size()} from {@code Map.of()})
     * cannot be invoked from outside their package, and the module system blocks {@code setAccessible}. Instead,
     * this walks the supertypes for a public, exported declaration of the same signature ({@code Map.size()}).
     * Invoking that still dispatches to the runtime implementation.
     */
    public static Method accessibleMethod(Method method) {
        if (isAccessible(method.getDeclaringClass())) return method;
        // static methods are not inherited, so a supertype's version would be a different method
        if (Modifier.isStatic(method.getModifiers())) return null;
        var queue = new ArrayDeque<Class<?>>();
        var seen = new HashSet<Class<?>>();
        queue.add(method.getDeclaringClass());
        while (!queue.isEmpty()) {
            Class<?> c = queue.poll();
            if (!seen.add(c)) continue;
            if (c != method.getDeclaringClass() && isAccessible(c)) {
                try {
                    return c.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    // keep looking
                }
            }
            if (c.getSuperclass() != null) queue.add(c.getSuperclass());
            queue.addAll(List.of(c.getInterfaces()));
        }
        return null;
    }

    /**
     * Returns {@code field} if it can be read reflectively, or {@code null} if it is declared on a class
     * outside our reach. Unlike methods, fields are not overridden, so there is no supertype to fall back to.
     */
    public static Field accessibleField(Field field) {
        return isAccessible(field.getDeclaringClass()) ? field : null;
    }

    private static boolean isAccessible(Class<?> c) {
        if (!Modifier.isPublic(c.getModifiers())) return false;
        Class<?> outer = c.getEnclosingClass();
        if (outer != null && !isAccessible(outer)) return false;
        return c.getModule().isExported(c.getPackageName(), NotchJavaMethodUtils.class.getModule());
    }

    public static <T extends Executable> BestMatch<T> getBestMatch(NotchRuntime runtime, List<NotchType> args, List<T> executables, boolean allowImplicitRuntime) {
        int minCost = MAX_VALUE;
        boolean minHasImplicitRuntime = false;
        BestMatch<T> bestMatch = null;
        for (T executable : executables) {
            boolean methodHasImplicitRuntime = false;
            if (allowImplicitRuntime && executable.getParameterCount() > 0 && NotchRuntime.class.isAssignableFrom(executable.getParameterTypes()[0])) {
                methodHasImplicitRuntime = true;
            }

            var distance = distanceFromValues(runtime, executable, args, methodHasImplicitRuntime);
            if (distance == null) continue;

            var match = new BestMatch<T>(runtime, executable, args, distance.paramTypes(), distance.coercers(), methodHasImplicitRuntime);
            boolean isBest = false;
            if (bestMatch == null || distance.cost < minCost) {
                isBest = true;
            } else if (distance.cost == minCost) {
                String str1 = signature(bestMatch.executable, minHasImplicitRuntime);
                String str2 = signature(executable, methodHasImplicitRuntime);
                isBest = str1.compareTo(str2) < 0;
            }

            if (isBest) {
                minCost = distance.cost();
                minHasImplicitRuntime = methodHasImplicitRuntime;
                bestMatch = match;

                if (minCost == 0) break;
            }
        }
        return bestMatch;
    }

    public record MethodDistance(int cost, List<NotchType> paramTypes, List<NotchTypeCoercer> coercers) {
    }

    public static MethodDistance distanceFromValues(NotchRuntime runtime, Executable executable, List<NotchType> argTypes, boolean hasImplicitRuntime) {
        Class<?>[] parameterTypes = executable.getParameterTypes();
        int skip = hasImplicitRuntime ? 1 : 0;
        if (argTypes.size() != parameterTypes.length - skip) {
            return null;
        }
        var coercers = new BetterList<NotchTypeCoercer>(argTypes.size());
        var paramTypes = new BetterList<NotchType>(argTypes.size());
        int distance = 0;
        for (int i = 0; i < argTypes.size(); i++) {
            NotchType argType = argTypes.get(i);
            NotchType paramType = NotchTypeSystem.getType(parameterTypes[i + skip]);
            var result = runtime.getCoercer(argType, paramType);
            if (result == null) return null;
            distance += result.cost();

            paramTypes.add(paramType);
            coercers.add(result.coercer());
        }
        return new MethodDistance(distance, paramTypes, coercers);
    }

    private static String signature(Executable executable, boolean hasImplicitRuntime) {
        return new BetterList<>(executable.getParameterTypes()).skip(hasImplicitRuntime ? 1 : 0).map(Class::getName).toString(";");
    }

    public static Object invoke(Object root, Span callsite, NotchRuntime runtime, List<Method> methods, List<Object> args, List<Span> argSpan, boolean allowImplicitRuntime) {
        var argTypes = NotchTypeSystem.getRuntimeTypes(args);
        var bestMatch = getBestMatch(runtime, argTypes, methods, allowImplicitRuntime);
        if (bestMatch == null) {
            String argTypesString = BetterList.better(args).map(NotchRuntime::className).toString(", ");
            throw new IllegalArgumentException("Could not find compatible method with args (" + argTypesString + ")");
        }

        Object[] argValues = bestMatch.coerceArgValues(args, argSpan);
        try (var _guard = runtime.showStackFrames()) {
            Object result;
            try {
                result = bestMatch.executable.invoke(root, argValues);
            } catch (InvocationTargetException e) {
                var cause = e.getCause();
                var trace = Thread.currentThread().getStackTrace();
                var st = runtime.getStackTrace(trace[2], cause);
                final var d = new Diagnostic();
                d.highlight(callsite);
                var msg = cause.getMessage();
                if (msg != null) d.note(msg);
                throw new NotchRuntimeException(st, d, cause);
            } catch (IllegalAccessException e) {
                throw rethrow(e);
            }
            if (bestMatch.executable.getReturnType() == Void.TYPE) {
                return NotchRuntime.UNDEFINED;
            }
            return result;
        }
    }

    public static Object newInstance(Span callsite, NotchRuntime runtime, List<Constructor> constructors, List<Object> args, List<Span> argSpan, boolean allowImplicitRuntime) {
        var argTypes = NotchTypeSystem.getRuntimeTypes(args);
        var bestMatch = getBestMatch(runtime, argTypes, constructors, allowImplicitRuntime);
        if (bestMatch == null) {
            throw new IllegalArgumentException("Unable to invoke constructor with arguments: " + argTypes.map(NotchType::getDisplayName).toString(";"));
        }

        Object[] argValues = bestMatch.coerceArgValues(args, argSpan);
        try (var _guard = runtime.showStackFrames()) {
            // TODO: callsite boundary
            var result = bestMatch.executable.newInstance(argValues);
            return result;
        } catch (InvocationTargetException e) {
            var cause = e.getCause();
            var trace = Thread.currentThread().getStackTrace();
            var st = runtime.getStackTrace(trace[2], cause);
            final var d = new Diagnostic();
            d.highlight(callsite);
            var msg = cause.getMessage();
            if (msg != null) d.note(msg);
            throw new NotchRuntimeException(st, d, cause);
        } catch (IllegalAccessException | InstantiationException e) {
            var stackTrace = runtime.currentStackTrace();
            final var d = new Diagnostic();
            d.highlight(callsite);
            d.note("unable to invoke constructor");
            d.note("caught exception: " + e.getMessage());
            throw new NotchRuntimeException(stackTrace, d, e);
        }
    }
}