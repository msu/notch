package bigsky.notch.types;


import bigsky.utils.BetterList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static bigsky.notch.runtime.NotchRuntime.UNDEFINED;
import static bigsky.notch.types.ChillJavaProperty.propertyNameFor;


public class JavaChillType implements ChillType, PropertyMissing {

    private final Class backingClass;
    private final ConcurrentHashMap<String, ChillJavaProperty> properties = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ChillMethod> methods = new ConcurrentHashMap<>();

    public JavaChillType(Class backingClass) {
        this.backingClass = backingClass;
    }

    @Override
    public ChillProperty getProperty(String propName) {
        return properties.computeIfAbsent(propName, this::resolveProperty);
    }

    @Override
    public String getDisplayName() {
        return backingClass.getName();
    }

    @Override
    public List<ChillMethod> getMethods() {

        BetterList<Method> methods = new BetterList<>(backingClass.getMethods());

        return methods.distinct(Method::getName)
                .map(method -> getMethod(method.getName()));

    }

    @Override
    public List<ChillProperty> getProperties() {

        BetterList<Method> methods = new BetterList<>(backingClass.getMethods());
        BetterList<ChillProperty> methodProperties = methods.filter(ChillJavaProperty::isPropertyMethod)
                .map(method -> getProperty(propertyNameFor(method)));

        BetterList<Field> fields = new BetterList<>(backingClass.getFields());
        BetterList<ChillProperty> fieldProperties = fields.map(field -> getProperty(field.getName()));

        return methodProperties.concat(fieldProperties);
    }

    @Override
    public List<ChillMethod> getDeclaredMethods() {

        BetterList<Method> methods = new BetterList<>(backingClass.getDeclaredMethods());

        return methods.distinct(Method::getName)
                .map(method -> getMethod(method.getName())).filter(Objects::nonNull);

    }

    @Override
    public List<ChillProperty> getDeclaredProperties() {

        BetterList<Method> methods = new BetterList<>(backingClass.getDeclaredMethods());
        BetterList<ChillProperty> methodProperties = methods.filter(ChillJavaProperty::isPropertyMethod)
                .map(method -> getProperty(propertyNameFor(method)));

        BetterList<Field> fields = new BetterList<>(backingClass.getDeclaredFields());
        BetterList<ChillProperty> chillProperties = fields.map(field -> getProperty(field.getName()));
        BetterList<ChillProperty> fieldProperties = chillProperties.filter(Objects::nonNull);

        return methodProperties.concat(fieldProperties);
    }

    @Override
    public Class getBackingClass() {
        return backingClass;
    }

    @Override
    public ChillMethod getMethod(String methodName) {
        return methods.computeIfAbsent(methodName, this::resolveMethod);
    }

    public ChillJavaProperty resolveProperty(String propName) {
        var chillJavaProperty = new ChillJavaProperty(backingClass, propName);
        if (chillJavaProperty.isValid()) {
            return chillJavaProperty;
        } else {
            return null;
        }
    }

    public ChillMethod resolveMethod(String methodName) {
        var javaMethod = new ChillJavaMethod(methodName, backingClass);
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
        ChillProperty property = getProperty(propName);
        if (property != null && property.isStatic()) {
            return property.get(null);
        } else {
            return UNDEFINED;
        }
    }
}
