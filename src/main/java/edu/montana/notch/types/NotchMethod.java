package edu.montana.notch.types;

public interface NotchMethod extends NotchFunction {
    String getDisplayName();

    String getQualifiedName();

    boolean isStatic();

    boolean isPublic();
    NotchType getDeclaringType();
}
