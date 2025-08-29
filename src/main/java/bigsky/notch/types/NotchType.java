package bigsky.notch.types;

import java.util.List;

public interface NotchType {
    NotchMethod getMethod(String propName);
    NotchProperty getProperty(String propName);
    String getDisplayName();
    List<NotchMethod> getMethods();
    List<NotchProperty> getProperties();
    List<NotchMethod> getDeclaredMethods();
    List<NotchProperty> getDeclaredProperties();
    Class getBackingClass();
}
