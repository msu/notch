package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.BetterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
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
        this.owner = owner;
        this.methodName = methodName;
        this.root = root;

        boolean onlyStatic = root == null;
        methods = new BetterList<>();
        variants = new BetterList<>();
        for (var method : owner.getMethods()) {
            if (!method.getName().equals(methodName)) continue;
            if (Modifier.isStatic(method.getModifiers()) != onlyStatic) continue;
            try {
                method.setAccessible(true);
                methods.add(method);
            } catch (SecurityException e) {
                log.error("Cannot access method {}.{}()", owner.getName(), methodName);
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
