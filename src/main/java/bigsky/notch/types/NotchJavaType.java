package bigsky.notch.types;


import bigsky.utils.BetterList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static bigsky.notch.runtime.NotchRuntime.UNDEFINED;
import static bigsky.notch.types.NotchJavaProperty.propertyNameFor;


public class NotchJavaType implements NotchType, PropertyMissing {

    private final Class backingClass;
    private final ConcurrentHashMap<String, NotchJavaProperty> properties = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NotchMethod> methods = new ConcurrentHashMap<>();

    public NotchJavaType(Class backingClass) {
        this.backingClass = backingClass;
    }

    @Override
    public NotchProperty getProperty(String propName) {
        return properties.computeIfAbsent(propName, this::resolveProperty);
    }

    @Override
    public String getDisplayName() {
        return backingClass.getName();
    }

    @Override
    public List<NotchMethod> getMethods() {

        BetterList<Method> methods = new BetterList<>(backingClass.getMethods());

        return methods.distinct(Method::getName)
                .map(method -> getMethod(method.getName()));

    }

    @Override
    public List<NotchProperty> getProperties() {

        BetterList<Method> methods = new BetterList<>(backingClass.getMethods());
        BetterList<NotchProperty> methodProperties = methods.filter(NotchJavaProperty::isPropertyMethod)
                .map(method -> getProperty(propertyNameFor(method)));

        BetterList<Field> fields = new BetterList<>(backingClass.getFields());
        BetterList<NotchProperty> fieldProperties = fields.map(field -> getProperty(field.getName()));

        return methodProperties.concat(fieldProperties);
    }

    @Override
    public List<NotchMethod> getDeclaredMethods() {

        BetterList<Method> methods = new BetterList<>(backingClass.getDeclaredMethods());

        return methods.distinct(Method::getName)
                .map(method -> getMethod(method.getName())).filter(Objects::nonNull);

    }

    @Override
    public List<NotchProperty> getDeclaredProperties() {

        BetterList<Method> methods = new BetterList<>(backingClass.getDeclaredMethods());
        BetterList<NotchProperty> methodProperties = methods.filter(NotchJavaProperty::isPropertyMethod)
                .map(method -> getProperty(propertyNameFor(method)));

        BetterList<Field> fields = new BetterList<>(backingClass.getDeclaredFields());
        BetterList<NotchProperty> chillProperties = fields.map(field -> getProperty(field.getName()));
        BetterList<NotchProperty> fieldProperties = chillProperties.filter(Objects::nonNull);

        return methodProperties.concat(fieldProperties);
    }

    @Override
    public Class getBackingClass() {
        return backingClass;
    }

    @Override
    public NotchMethod getMethod(String methodName) {
        return methods.computeIfAbsent(methodName, this::resolveMethod);
    }

    public NotchJavaProperty resolveProperty(String propName) {
        var chillJavaProperty = new NotchJavaProperty(backingClass, propName);
        if (chillJavaProperty.isValid()) {
            return chillJavaProperty;
        } else {
            return null;
        }
    }

    public NotchMethod resolveMethod(String methodName) {
        var javaMethod = new NotchJavaMethod(methodName, backingClass);
        if (javaMethod.isValid()) {
            return javaMethod;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "JavaChillType: " + backingClass.getName();
    }

    @Override
    public Object propertyMissing(String propName) {
        NotchProperty property = getProperty(propName);
        if (property != null && property.isStatic()) {
            return property.get(null);
        } else {
            return UNDEFINED;
        }
    }
}
