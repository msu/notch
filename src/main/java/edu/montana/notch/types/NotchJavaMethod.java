package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.Exceptions;
import edu.montana.notch.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.List;

import static edu.montana.notch.util.Exceptions.rethrow;
import static edu.montana.notch.util.Pair.pair;

public class NotchJavaMethod implements NotchMethod {

    private static final Logger log = LoggerFactory.getLogger(NotchJavaMethod.class);
    private final BetterList<Method> javaMethods;
    private final String name;
    private final Class backingClass;
    private final boolean staticMethod;

    public NotchJavaMethod(String methodName, Class backingClass, boolean staticMethod, List<Method> candidates) {
        this.name = methodName;
        this.staticMethod = staticMethod;
        this.backingClass = backingClass;
        this.javaMethods = new BetterList<>();
        var signatures = new HashSet<List<Class<?>>>();
        for (Method m : candidates) {
            Method accessible = NotchJavaMethodUtils.accessibleMethod(m);
            if (accessible == null) {
                log.debug("Cannot access method {}.{}()", backingClass.getName(), methodName);
                continue;
            }
            if (signatures.add(List.of(accessible.getParameterTypes()))) {
                javaMethods.add(accessible);
            }
        }
        if (javaMethods.isEmpty()) {
            throw new IllegalStateException("No accessible methods named " + methodName + " found for " + backingClass.getName());
        }
    }

    @Override
    public Object invoke(NotchRuntime runtime, Object rootVal, Span callsite, List<Object> args, List<Span> argSpan) {
        return NotchJavaMethodUtils.invoke(rootVal, callsite, runtime, javaMethods, args, argSpan, false);
    }

    @Override
    public String getName() {
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
        return NotchTypeSystem.getType(this.backingClass);
    }

    @Override
    public boolean canInvokeWith(NotchRuntime runtime, NotchType root, List<NotchType> args) {
        var bestMethod = NotchJavaMethodUtils.getBestMatch(runtime, args, javaMethods, false);
        return bestMethod != null;
    }

    @Override
    public List<NotchFunctionDescription> getVariants() {
        var out = new BetterList<NotchFunctionDescription>();
        for (var method : javaMethods) {
            var params = new BetterList<Pair<NotchType, String>>();
            Parameter[] parameters = method.getParameters();
            for (Parameter param : parameters) {
                var name = param.getName();
                var ty = NotchTypeSystem.getType(param.getType());
                params.add(pair(ty, name));
            }
            final var rt = NotchTypeSystem.getType(method.getReturnType());
            out.add(new NotchFunctionDescription(rt, params));
        }
        return out;
    }
}
