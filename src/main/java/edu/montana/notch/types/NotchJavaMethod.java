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
import java.util.List;

import static edu.montana.notch.util.Exceptions.rethrow;
import static edu.montana.notch.util.Pair.pair;
import static java.lang.Integer.MAX_VALUE;

public class NotchJavaMethod implements NotchMethod {

    private static final Logger log = LoggerFactory.getLogger(NotchJavaMethod.class);
    private final BetterList<Method> javaMethods;
    private final String name;
    private final Class backingClass;
    private final boolean staticMethod;

    public NotchJavaMethod(String methodName, Class backingClass, boolean staticMethod) {
        this.name = methodName;
        this.staticMethod = staticMethod;
        this.backingClass = backingClass;
        this.javaMethods = new BetterList<>();
        Method[] allMethods = backingClass.getMethods();
        for (Method m : allMethods) {
            if (m.getName().equals(methodName) && Modifier.isStatic(m.getModifiers()) == staticMethod) {
                try {
                    m.setAccessible(true);
                    javaMethods.add(m);
                } catch (SecurityException e) {
                    log.error("Cannot access method {}.{}()", backingClass.getName(), methodName);
                }
            }
        }
    }

    @Override
    public Object invoke(NotchRuntime runtime, Object rootVal, List<Object> args, List<Span> argSpan) {
        try {
            if (javaMethods.isEmpty()) {
                throw new NoSuchMethodException("No methods named " + this.name + " were found on " + backingClass.getName());
            }
            Method method = bestMatch(runtime, args);
            if (method == null) {
                String argTypes = BetterList.better(args).map(NotchRuntime::className).toString(",");
                throw new IllegalArgumentException("Could not find compatible method with args (" + argTypes + ")");
            }
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                if (arg != null) {
                    Class<?> parameterType = method.getParameterTypes()[i];
                    Class<?> runtimeType = arg.getClass();
                    if (!parameterType.isAssignableFrom(runtimeType)) {
                        var value = runtime.coerce(parameterType, arg, argSpan.get(i));
                        args.set(i, value);
                    }
                }
            }
            return method.invoke(rootVal, args.toArray());
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = e.getCause();
                // remove reflection internals
                e.setStackTrace(Exceptions.replaceInStackTrace(e.getStackTrace(),
                        (ste) -> ste.getClassName().startsWith("java.") ||
                                ste.getClassName().startsWith("jdk.") ||
                                ste.getClassName().startsWith("javax.") ||
                                ste.getClassName().contains("NotchJavaMethod"),
                        new RuntimeException()));
            }
            throw rethrow(e);
        }
    }

    private Method bestMatch(NotchRuntime runtime, List<Object> argValues) {
        return getBestMatch(runtime, argValues, javaMethods);
    }

    public static <T extends Executable> T getBestMatch(NotchRuntime runtime, List<Object> argValues, List<T> executables) {
        int closestDistance = MAX_VALUE;
        T bestMatch = null;
        for (T javaMethod : executables) {
            int distance = distanceFromValues(runtime, javaMethod, argValues);
            if (distance == MAX_VALUE) {
                continue;
            } else if (distance < closestDistance) {
                closestDistance = distance;
                bestMatch = javaMethod;
            } else if (distance == closestDistance) {
                // stable resolution by using string comparison
                String str1 = new BetterList<>(bestMatch.getParameterTypes()).map(Class::getName).toString("-");
                String str2 = new BetterList<>(javaMethod.getParameterTypes()).map(Class::getName).toString("-");
                if (str1.compareTo(str2) < 0) {
                    bestMatch = javaMethod;
                }
            }
        }
        return bestMatch;
    }

    public static int distanceFromValues(NotchRuntime runtime, Executable executable, List<Object> runtimeValues) {
        Class<?>[] parameterTypes = executable.getParameterTypes();
        if (runtimeValues.size() == parameterTypes.length) {
            int distance = 0;
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                Object o = runtimeValues.get(i);
                if (o != null) {
                    Class<?> runtimeClass = o.getClass();
                    int paramDistance = distanceTo(runtime, runtimeClass, parameterType);
                    if (paramDistance == MAX_VALUE) {
                        return MAX_VALUE;
                    }
                    distance += paramDistance;
                }
            }
            return distance;
        }
        return MAX_VALUE;
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
        return staticMethod;
    }

    @Override
    public boolean isPublic() {
        return javaMethods.hasMatch((m) -> Modifier.isPublic(m.getModifiers()));
    }

    @Override
    public boolean canInvokeWith(NotchRuntime runtime, List<Object> args) {
        Method method = bestMatch(runtime, args);
        return method != null;
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
    public List<Pair<List<Pair<String, NotchType>>, NotchType>> getMethodVariants() {
        var out = new BetterList<Pair<List<Pair<String, NotchType>>, NotchType>>();
        for (var method : javaMethods) {
            List<Pair<String, NotchType>> params = new BetterList<>();
            Parameter[] parameters = method.getParameters();
            for (Parameter param : parameters) {
                var name = param.getName();
                var ty = TypeSystem.getType(param.getType());
                params.add(pair(name, ty));
            }
            final var rt = TypeSystem.getType(method.getReturnType());
            out.add(pair(params, rt));
        }
        return out;
    }
}
