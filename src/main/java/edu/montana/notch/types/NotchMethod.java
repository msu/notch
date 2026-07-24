package edu.montana.notch.types;

import edu.montana.notch.runtime.NotchRuntime;
import java.util.List;

public interface NotchMethod {
    Object invoke(NotchRuntime runtime, Object rootVal, List<Object> args);

    String getName();

    String getDisplayName();

    String getQualifiedName();

    boolean isStatic();

    boolean isPublic();

    boolean canInvokeWith(List<Object> args);
}
