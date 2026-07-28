package edu.montana.notch.types;

import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.Pair;

import java.util.List;

public interface NotchMethod {
    Object invoke(NotchRuntime runtime, Object rootVal, List<Object> args);

    String getName();

    String getDisplayName();

    String getQualifiedName();

    boolean isStatic();

    boolean isPublic();

    boolean canInvokeWith(NotchRuntime runtime, List<Object> args);

    default List<Pair<List<Pair<String, NotchType>>, NotchType>> getMethodVariants() {
        // by default methods do not have to declare their args & return types
        return List.of();
    }
}
