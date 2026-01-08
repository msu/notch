package bigsky.notch.types;

import bigsky.notch.types.coercions.Coercion;
import bigsky.utils.BetterList;
import bigsky.utils.Text;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static bigsky.utils.Exceptions.rethrow;
import static java.lang.Integer.MAX_VALUE;

public class NotchJavaMethod implements NotchMethod {

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
            if (m.getName().equals(methodName) &&  Modifier.isStatic(m.getModifiers()) == staticMethod) {
                m.setAccessible(true);
                javaMethods.add(m);
            }
        }
    }

    @Override
    public Object invoke(Object rootVal, List<Object> args) {
        try {
            if (javaMethods.isEmpty()) {
                throw new NoSuchMethodException("No methods named " + this.name + " were found on " + backingClass.getName());
            }
            Method method = bestMatch(args);
            if (method == null) {
                String argTypes = BetterList.better(args).map(Text::className).join(",");
                throw new IllegalArgumentException("Could not find compatible method with args (" + argTypes + ")");
            }
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                Class<?> parameterType = method.getParameterTypes()[i];
                if (arg != null) {
                    Class<?> runtimeType = arg.getClass();
                    if (!parameterType.isAssignableFrom(runtimeType)) {
                        Coercion coercer = Coercion.resolve(runtimeType, parameterType);
                        if (coercer == null) {
                            throw new IllegalArgumentException("Could not figure out how to convert argument " + i + " of type " + runtimeType.getName() + " to " + parameterType.getName());
                        } else {
                            args.set(i, coercer.coerce(arg));
                        }
                    }
                }
            }
            return method.invoke(rootVal, args.toArray());
        } catch (Exception e) {
            throw rethrow(e);
        }
    }

    private Method bestMatch(List<Object> argValues) {
        return getBestMatch(argValues, javaMethods);
    }

    public static <T extends Executable> T getBestMatch(List<Object> argValues, List<T> executables) {
        int closestDistance = MAX_VALUE;
        T bestMatch = null;
        for (T javaMethod : executables) {
            int distance = distanceFromValues(javaMethod, argValues);
            if (distance == MAX_VALUE) {
                continue;
            } else if (distance < closestDistance) {
                closestDistance = distance;
                bestMatch = javaMethod;
            } else if (distance == closestDistance) {
                // stable resolution by using string comparison
                String str1 = new BetterList<>(bestMatch.getParameterTypes()).map(Class::getName).join("-");
                String str2 = new BetterList<>(javaMethod.getParameterTypes()).map(Class::getName).join("-");
                if (str1.compareTo(str2) < 0) {
                    bestMatch = javaMethod;
                }
            }
        }
        return bestMatch;
    }

    public static int distanceFromValues(Executable executable, List<Object> runtimeValues) {
        Class<?>[] parameterTypes = executable.getParameterTypes();
        if(runtimeValues.size() == parameterTypes.length) {
            int distance = 0;
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                Object o = runtimeValues.get(i);
                if (o != null) {
                    Class<?> runtimeClass = o.getClass();
                    int paramDistance = distanceTo(runtimeClass, parameterType);
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
    public boolean canInvokeWith(List<Object> args) {
        Method method = bestMatch(args);
        return method != null;
    }

    private static int distanceTo(Class<?> from, Class<?> to) {
        if (from == null) {
            return MAX_VALUE;
        }
        if (from.equals(to)) {
            return 0;
        }
        if (Arrays.asList(from.getInterfaces()).contains(to)) {
            return 1;
        }
        Coercion coercion = Coercion.resolve(from, to);
        if (coercion != null) {
            return coercion.getRank();
        }
        int distanceToSuperclass = distanceTo(from.getSuperclass(), to);
        if (distanceToSuperclass == MAX_VALUE) {
            return MAX_VALUE;
        } else {
            return 2 + distanceToSuperclass;
        }
    }

}
