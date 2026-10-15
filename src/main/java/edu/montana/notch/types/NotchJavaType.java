package edu.montana.notch.types;


import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.BetterSet;
import edu.montana.notch.util.Text;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static edu.montana.notch.types.NotchJavaProperty.isPropertyMethod;
import static edu.montana.notch.types.NotchJavaProperty.propertyNameFor;
import static edu.montana.notch.util.Exceptions.safelyEval;


public class NotchJavaType implements NotchType {

    private final Class backingClass;
    private final ConcurrentHashMap<String, NotchJavaField> fields = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NotchJavaProperty> properties = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NotchJavaProperty> staticProperties = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NotchMethod> methods = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NotchFunction> staticMethods = new ConcurrentHashMap<>();
    private List<NotchType> supertypes;
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
            try {
                fields.put(declaredField.getName(), new NotchJavaField(declaredField));
            } catch (InaccessibleObjectException e) {
                // ignore inaccessible fields
            }
        }
    }

    private void initConstructors() {
        this.constructors = new BetterList<>(backingClass.getDeclaredConstructors())
                .tap(constructor -> {
                    try {
                        constructor.setAccessible(true);
                    } catch (java.lang.reflect.InaccessibleObjectException ignored) {
                        // JPMS-locked non-public constructor (e.g. private
                        // String ctors). Public ones are already accessible;
                        // leave non-public ones in their default state.
                    }
                });
    }

    private void initProperties() {
        for (Method method : backingClass.getMethods()) {
            if (isPropertyMethod(method)) {
                String propName = propertyNameFor(method);
                NotchJavaProperty property = new NotchJavaProperty(backingClass, propName, Modifier.isStatic(method.getModifiers()));
                addProperty(property);
                // Also allow addressing the property by the original method
                // name and its snake_case form — e.g. foo.get_null or
                // foo.getNull both resolve to getNull() the same way foo.null
                // already does.
                var map = property.isStatic() ? staticProperties : properties;
                map.putIfAbsent(method.getName(), property);
                map.putIfAbsent(Text.snakeCase(method.getName()), property);
            }
        }
        for (Field field : backingClass.getFields()) {
            NotchJavaProperty value = new NotchJavaProperty(backingClass, field.getName(), Modifier.isStatic(field.getModifiers()));
            addProperty(value);
        }
    }

    private void initMethods() {
        for (Method method : backingClass.getMethods()) {
            boolean staticMethod = Modifier.isStatic(method.getModifiers());
            if (staticMethod && !staticMethods.containsKey(method.getName())) {
                NotchFunction notchFunction = new NotchBoundJavaFunction(backingClass, method.getName());
                staticMethods.put(method.getName(), notchFunction);
            } else if (!staticMethod && !methods.containsKey(method.getName())) {
                NotchJavaMethod notchMethod = new NotchJavaMethod(method.getName(), backingClass, false);
                methods.put(method.getName(), notchMethod);
            }
        }
    }

    private void addProperty(NotchJavaProperty property) {
        if (property.isStatic()) {
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
        if (backingClass == Class.class) {
            return "class " + backingClass.getName();
        }
        if (backingClass.isArray()) {
            var ty = NotchTypeSystem.getType(backingClass.getComponentType());
            return ty.getDisplayName() + "[]";
        }
        return backingClass.getName();
    }

    @Override
    public String getSimpleName() {
        return backingClass.getSimpleName();
    }

    @Override
    public String getNamespace() {
        return backingClass.getPackageName();
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
    public List<NotchFunction> getStaticMethods() {
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
    public List<NotchType> getDirectSupertypes() {
        if (supertypes != null) return supertypes;
        var out = new BetterList<NotchType>();
        // getSuperclass() is null for Object, interfaces and primitives. Only
        // primitives really have nowhere to go — interfaces are still
        // assignable to Object, so put it back for them.
        Class<?> superclass = backingClass.getSuperclass();
        if (superclass != null) {
            out.add(NotchTypeSystem.getType(superclass));
        } else if (backingClass.isInterface()) {
            out.add(NotchTypeSystem.getType(Object.class));
        }
        // Array covariance is not reported by getSuperclass(): String[] says
        // Object, not Object[].
        if (backingClass.isArray()
                && !backingClass.getComponentType().isPrimitive()
                && backingClass != Object[].class) {
            out.add(NotchTypeSystem.getType(Object[].class));
        }
        for (var iface : backingClass.getInterfaces()) {
            out.add(NotchTypeSystem.getType(iface));
        }
        supertypes = out;
        return supertypes;
    }

    @Override
    public Set<NotchType> getAllSupertypes() {
        var out = new BetterSet<NotchType>();
        var supertypes = getDirectSupertypes();
        out.addAll(supertypes);
        for (var ty : supertypes) {
            out.addAll(ty.getAllSupertypes());
        }
        return out;
    }

    @Override
    public Object newInstance(NotchRuntime runtime, Span callsite, List<Object> args, List<Span> argSpan) {
        Object value = NotchJavaMethodUtils.newInstance(callsite, runtime, constructors, args, argSpan, false);
        return value;
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
    public NotchFunction getStaticMethod(String propName) {
        return staticMethods.get(propName);
    }

    @Override
    public String toString() {
        return "NotchJavaType: " + backingClass.getName();
    }

}
