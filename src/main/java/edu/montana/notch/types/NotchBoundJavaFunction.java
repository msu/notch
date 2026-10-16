package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.BetterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class NotchBoundJavaFunction implements NotchFunction {
    private static final Logger log = LoggerFactory.getLogger(NotchBoundJavaFunction.class);
    private final Class<?> owner;
    private final String methodName;
    private final Object root;
    private final BetterList<Method> methods;
    private BetterList<NotchFunctionDescription> variants;

    public NotchBoundJavaFunction(Class<?> owner, String methodName) {
        this(owner, methodName, null);
    }

    public NotchBoundJavaFunction(Class<?> owner, String methodName, Object root) {
        this(owner, methodName, root, Arrays.stream(owner.getMethods()).filter(m -> m.getName().equals(methodName)).toList());
    }

    public NotchBoundJavaFunction(Class<?> owner, String methodName, Object root, List<Method> candidates) {
        this.owner = owner;
        this.methodName = methodName;
        this.root = root;

        boolean onlyStatic = root == null;
        methods = new BetterList<>();
        variants = new BetterList<>();
        var signatures = new HashSet<List<Class<?>>>();
        for (var method : candidates) {
            if (Modifier.isStatic(method.getModifiers()) != onlyStatic) continue;
            Method accessible = NotchJavaMethodUtils.accessibleMethod(method);
            if (accessible == null) {
                log.debug("Cannot access method {}.{}()", owner.getName(), methodName);
                continue;
            }
            if (signatures.add(List.of(accessible.getParameterTypes()))) {
                methods.add(accessible);
            }
        }

        if (methods.isEmpty()) {
            throw new IllegalArgumentException("No methods found for " + owner.getSimpleName() + "." + methodName);
        }
    }

    @Override
    public String getName() {
        return methodName;
    }

    @Override
    public Object invoke(NotchRuntime runtime, Span callsite, List<Object> args, List<Span> argSpan) {
        Object value = NotchJavaMethodUtils.invoke(this.root, callsite, runtime, methods, args, argSpan, true);
        return value;
    }

    @Override
    public boolean canInvokeWith(NotchRuntime runtime, List<NotchType> args) {
        return null != NotchJavaMethodUtils.getBestMatch(runtime, args, methods, true);
    }

    @Override
    public List<NotchFunctionDescription> getVariants() {
        if (variants == null) {
            variants = new BetterList<>();
            for (var method : methods) {
                final var variant = new NotchFunctionDescription(method);
                variants.add(variant);
            }
        }
        return Collections.unmodifiableList(variants);
    }
}
