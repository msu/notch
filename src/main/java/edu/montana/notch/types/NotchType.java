package edu.montana.notch.types;

import java.util.List;

public interface NotchType {
    NotchMethod getMethod(String propName);
    NotchMethod getStaticMethod(String propName);
    NotchProperty getProperty(String propName);
    NotchProperty getStaticProperty(String propName);
    String getDisplayName();
    String getSimpleName();
    List<NotchMethod> getMethods();
    List<NotchMethod> getStaticMethods();
    List<NotchProperty> getProperties();
    List<NotchProperty> getStaticProperties();
    Class getBackingClass();
    Object newInstance(Object[] args);
    NotchField getField(String name);
}
