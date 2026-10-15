package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;

public interface NotchMethod {
    String getName();

    String getQualifiedName();

    boolean isStatic();

    boolean isPublic();
    NotchType getDeclaringType();

    Object invoke(NotchRuntime runtime, Object root, Span callsite, List<Object> args, List<Span> argSpan);

    boolean canInvokeWith(NotchRuntime runtime, NotchType root, List<NotchType> args);
    default List<NotchFunctionDescription> getVariants() {
        // by default functions do not have to declare their args & return types
        return List.of();
    }
}
