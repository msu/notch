package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.Exceptions;
import edu.montana.notch.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.List;

import static edu.montana.notch.util.Exceptions.rethrow;
import static edu.montana.notch.util.Pair.pair;

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
            MethodResolver.coerceArguments(runtime, method, args, argSpan, 0);
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
        return MethodResolver.getBestMatch(runtime, argValues, javaMethods);
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
            for (Parameter param : parameters) {
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
