package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
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
            if (!m.getName().equals(methodName)) continue;
            if (Modifier.isStatic(m.getModifiers())) continue;
            if (m.getDeclaringClass() == Object.class) continue;
            try {
                m.setAccessible(true);
                javaMethods.add(m);
            } catch (SecurityException e) {
                log.error("Cannot access method {}.{}()", backingClass.getName(), methodName);
            }
        }
        if (javaMethods.isEmpty()) {
            throw new IllegalStateException();
        }
    }

    /**
     * Builtins may declare a leading {@link NotchRuntime} parameter; it is supplied implicitly by
     * {@link #invoke} and hidden from both overload matching and {@link #getVariants()}.
     */
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
            MethodResolver.coerceArguments(runtime, method, args, argSpan, skip);
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
        return MethodResolver.getBestMatch(runtime, argValues, candidates, NotchBuiltinMethod::implicitParamCount);
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