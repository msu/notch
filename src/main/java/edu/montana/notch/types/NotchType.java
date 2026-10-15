package edu.montana.notch.types;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;
import java.util.Set;

public interface NotchType {
    NotchMethod getMethod(String propName);

    NotchFunction getStaticMethod(String propName);

    NotchProperty getProperty(String propName);

    NotchProperty getStaticProperty(String propName);

    String getDisplayName();

    String getSimpleName();

    String getNamespace();

    List<NotchMethod> getMethods();

    List<NotchFunction> getStaticMethods();

    List<NotchProperty> getProperties();

    List<NotchProperty> getStaticProperties();

    Class getBackingClass();

    List<NotchType> getDirectSupertypes();

    Set<NotchType> getAllSupertypes();

    Object newInstance(NotchRuntime runtime, Span callsite, List<Object> args, List<Span> argSpan);

    NotchField getField(String name);
}
