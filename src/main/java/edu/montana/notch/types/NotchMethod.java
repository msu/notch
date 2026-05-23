package edu.montana.notch.types;

import java.util.List;

public interface NotchMethod {
    Object invoke(Object rootVal, List<Object> args);

    String getName();

    String getDisplayName();

    String getQualifiedName();

    boolean isStatic();

    boolean isPublic();

    boolean canInvokeWith(List<Object> args);
}
