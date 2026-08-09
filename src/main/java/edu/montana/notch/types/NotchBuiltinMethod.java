package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.coercions.CoercionPath;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.Exceptions;
import edu.montana.notch.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import static edu.montana.notch.util.Exceptions.rethrow;
import static edu.montana.notch.util.Pair.pair;
import static java.lang.Integer.MAX_VALUE;

/**
 * A method on a builtin root object. Unlike {@link NotchJavaMethod}, a builtin may declare a
 * leading {@link NotchRuntime} parameter: it is supplied by {@link #invoke} rather than by the
 * script, so it takes no part in overload matching and is hidden from {@link #getVariants()}.
 */
public class NotchBuiltinMethod implements NotchMethod {
    private static final Logger log = LoggerFactory.getLogger(NotchBuiltinMethod.class);
    private final BetterList<Method> javaMethods;
    private final String name;
    private final Class backingClass;

    public NotchBuiltinMethod(String methodName, Class backingClass) {
        this.name = methodName;
        this.backingClass = backingClass;
        this.javaMethods = new BetterList<>();
        for (Method m : backingClass.getMethods()) {
            if (m.getName().equals(methodName) && !Modifier.isStatic(m.getModifiers())) {
                try {
                    m.setAccessible(true);
                    javaMethods.add(m);
                } catch (SecurityException e) {
                    log.error("Cannot access method {}.{}()", backingClass.getName(), methodName);
                }
            }
        }
    }

    private static boolean hasImplicitRuntime(Executable executable) {
        Class<?>[] parameterTypes = executable.getParameterTypes();
        return parameterTypes.length > 0 && NotchRuntime.class.isAssignableFrom(parameterTypes[0]);
    }

    private static int implicitParamCount(Executable executable) {
        return hasImplicitRuntime(executable) ? 1 : 0;
    }

    @Override
    public Object invoke(NotchRuntime runtime, Object rootVal, List<Object> args, List<Span> argSpan) {
        try {
            Method method = bestMatch(runtime, args);
            if (method == null) {
                String argTypes = BetterList.better(args).map(NotchRuntime::className).toString(",");
                throw new IllegalArgumentException("Could not find compatible method with args (" + argTypes + ")");
            }
            int skip = implicitParamCount(method);
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                if (arg != null) {
                    Class<?> parameterType = method.getParameterTypes()[i + skip];
                    Class<?> runtimeType = arg.getClass();
                    if (!parameterType.isAssignableFrom(runtimeType)) {
                        var value = runtime.coerce(parameterType, arg, argSpan.get(i));
                        args.set(i, value);
                    }
                }
            }
            List<Object> callArgs;
            if (skip > 0) {
                callArgs = new ArrayList<>(args.size() + 1);
                callArgs.add(runtime);
                callArgs.addAll(args);
            } else {
                callArgs = args;
            }
            return method.invoke(rootVal, callArgs.toArray());
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = e.getCause();
                // remove reflection internals
                e.setStackTrace(Exceptions.replaceInStackTrace(e.getStackTrace(),
                        (ste) -> ste.getClassName().startsWith("java.") ||
                                ste.getClassName().startsWith("jdk.") ||
                                ste.getClassName().startsWith("javax.") ||
                                ste.getClassName().contains("NotchBuiltinMethod"),
                        new RuntimeException()));
            }
            throw rethrow(e);
        }
    }

    private Method bestMatch(NotchRuntime runtime, List<Object> argValues) {
        // a builtin declaring a runtime subtype (e.g. NotchTemplateRuntime) only applies when the
        // active runtime actually is one
        var candidates = javaMethods.filter((m) -> !hasImplicitRuntime(m) || m.getParameterTypes()[0].isInstance(runtime));
        int closestDistance = MAX_VALUE;
        Method bestMatch = null;
        for (Method javaMethod : candidates) {
            int distance = distanceFromValues(runtime, javaMethod, argValues);
            if (distance == MAX_VALUE) {
                continue;
            } else if (distance < closestDistance) {
                closestDistance = distance;
                bestMatch = javaMethod;
            } else if (distance == closestDistance) {
                // stable resolution by using string comparison
                String str1 = signature(bestMatch);
                String str2 = signature(javaMethod);
                if (str1.compareTo(str2) < 0) {
                    bestMatch = javaMethod;
                }
            }
        }
        return bestMatch;
    }

    private static String signature(Executable executable) {
        return new BetterList<>(executable.getParameterTypes())
                .skip(implicitParamCount(executable))
                .map(Class::getName)
                .toString("-");
    }

    private static int distanceFromValues(NotchRuntime runtime, Executable executable, List<Object> runtimeValues) {
        Class<?>[] parameterTypes = executable.getParameterTypes();
        int skip = implicitParamCount(executable);
        if (runtimeValues.size() != parameterTypes.length - skip) {
            return MAX_VALUE;
        }
        int distance = 0;
        for (int i = 0; i < runtimeValues.size(); i++) {
            Class<?> parameterType = parameterTypes[i + skip];
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

    private static int distanceTo(NotchRuntime runtime, Class<?> from, Class<?> to) {
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return javaMethods.get(0).getName() + "()";
    }

    @Override
    public String getQualifiedName() {
        var parent = javaMethods.get(0).getDeclaringClass();
        return parent.getName() + "#" + name;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public boolean isPublic() {
        return javaMethods.hasMatch((m) -> Modifier.isPublic(m.getModifiers()));
    }

    @Override
    public NotchType getDeclaringType() {
        return TypeRegistry.getType(this.backingClass);
    }

    @Override
    public boolean canInvokeWith(NotchRuntime runtime, List<Object> args) {
        Method method = bestMatch(runtime, args);
        return method != null;
    }

    @Override
    public List<NotchFunctionDescription> getVariants() {
        var out = new BetterList<NotchFunctionDescription>();
        for (var method : javaMethods) {
            var params = new BetterList<Pair<NotchType, String>>();
            Parameter[] parameters = method.getParameters();
            for (int i = implicitParamCount(method); i < parameters.length; i++) {
                Parameter param = parameters[i];
                var name = param.getName();
                var ty = TypeRegistry.getType(param.getType());
                params.add(pair(ty, name));
            }
            final var rt = TypeRegistry.getType(method.getReturnType());
            out.add(new NotchFunctionDescription(rt, params));
        }
        return out;
    }
}
