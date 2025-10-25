package bigsky.notch.types;


import bigsky.utils.BetterList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static bigsky.notch.types.NotchJavaProperty.isPropertyMethod;
import static bigsky.notch.types.NotchJavaProperty.propertyNameFor;
import static bigsky.utils.Exceptions.safelyEval;


public class NotchJavaType implements NotchType {

    private final Class backingClass;
    private final ConcurrentHashMap<String, NotchJavaField> fields = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NotchJavaProperty> properties = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NotchJavaProperty> staticProperties = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NotchMethod> methods = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NotchMethod> staticMethods = new ConcurrentHashMap<>();
    private BetterList<Constructor> constructors;

    public NotchJavaType(Class backingClass) {
        this.backingClass = backingClass;
        initProperties();
        initMethods();
        initFields();
        initConstructors();
    }

    private void initFields() {
        for (Field declaredField : backingClass.getDeclaredFields()) {
            fields.put(declaredField.getName(), new NotchJavaField(declaredField));
        }
    }

    private void initConstructors() {
        this.constructors = new BetterList<>(backingClass.getDeclaredConstructors())
                .tapEach(constructor -> constructor.setAccessible(true));
    }

    private void initProperties() {
        for (Method method : backingClass.getMethods()) {
            if(isPropertyMethod(method)){
                String propName = propertyNameFor(method);
                NotchJavaProperty property = new NotchJavaProperty(backingClass, propName, Modifier.isStatic(method.getModifiers()));
                addProperty(property);
            }
        }
        for(Field field : backingClass.getFields()) {
            NotchJavaProperty value = new NotchJavaProperty(backingClass, field.getName(), Modifier.isStatic(field.getModifiers()));
            addProperty(value);
        }
    }

    private void initMethods() {
        for (Method method : backingClass.getMethods()) {
            boolean staticMethod = Modifier.isStatic(method.getModifiers());
            if(staticMethod && !staticMethods.containsKey(method.getName())){
                NotchJavaMethod notchMethod = new NotchJavaMethod(method.getName(), backingClass, true);
                staticMethods.put(method.getName(), notchMethod);
            } else if (!staticMethod && !methods.containsKey(method.getName())){
                NotchJavaMethod notchMethod = new NotchJavaMethod(method.getName(), backingClass, false);
                methods.put(method.getName(), notchMethod);
            }
        }
    }

    private void addProperty(NotchJavaProperty property) {
        if(property.isStatic()){
            staticProperties.putIfAbsent(property.getName(), property);
            staticProperties.putIfAbsent(property.getAlternateName(), property);
        } else {
            properties.putIfAbsent(property.getName(), property);
            properties.putIfAbsent(property.getAlternateName(), property);
        }
    }

    @Override
    public NotchProperty getProperty(String propName) {
        return properties.get(propName);
    }

    @Override
    public NotchProperty getStaticProperty(String propName) {
        return staticProperties.get(propName);
    }

    @Override
    public String getDisplayName() {
        return backingClass.getName();
    }

    @Override
    public List<NotchMethod> getMethods() {
        return new BetterList<>(methods.values());
    }

    @Override
    public List<NotchProperty> getProperties() {
        return new BetterList<>(properties.values());
    }

    @Override
    public List<NotchMethod> getStaticMethods() {
        return new BetterList<>(staticMethods.values());
    }

    @Override
    public List<NotchProperty> getStaticProperties() {
        return new BetterList<>(staticProperties.values());
    }

    @Override
    public Class getBackingClass() {
        return backingClass;
    }

    @Override
    public Object newInstance(Object[] args) {
        Constructor bestMatch = NotchJavaMethod.getBestMatch(Arrays.asList(args), constructors);
        if(bestMatch != null){
            return safelyEval(() -> bestMatch.newInstance(args));
        } else {
            throw new IllegalArgumentException("No constructor available for values " + Arrays.asList(args));
        }
    }

    @Override
    public NotchField getField(String name) {
        return fields.get(name);
    }

    @Override
    public NotchMethod getMethod(String methodName) {
        return methods.get(methodName);
    }

    @Override
    public NotchMethod getStaticMethod(String propName) {
        return staticMethods.get(propName);
    }

    @Override
    public String toString() {
        return "NotchJavaType: " + backingClass.getName();
    }

}
