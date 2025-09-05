package bigsky.notch.types;

import java.util.WeakHashMap;

public class TypeSystem {
    public static final NotchJavaType BOOLEAN = new NotchJavaType(Boolean.TYPE);
    public static final NotchJavaType BYTE = new NotchJavaType(Byte.TYPE);
    public static final NotchJavaType CHAR = new NotchJavaType(Character.TYPE);
    public static final NotchJavaType SHORT = new NotchJavaType(Short.TYPE);
    public static final NotchJavaType INT = new NotchJavaType(Integer.TYPE);
    public static final NotchJavaType LONG = new NotchJavaType(Long.TYPE);
    public static final NotchJavaType FLOAT = new NotchJavaType(Float.TYPE);
    public static final NotchJavaType DOUBLE = new NotchJavaType(Double.TYPE);

    private static final WeakHashMap<String, NotchType> TYPESYSTEM_CACHE = new WeakHashMap<>();
    static {
        synchronized (TYPESYSTEM_CACHE) {
            // put primitives into type system by name because they do not resolve normally
            TYPESYSTEM_CACHE.put("boolean", BOOLEAN);
            TYPESYSTEM_CACHE.put("byte", BYTE);
            TYPESYSTEM_CACHE.put("char", CHAR);
            TYPESYSTEM_CACHE.put("short", SHORT);
            TYPESYSTEM_CACHE.put("int", INT);
            TYPESYSTEM_CACHE.put("long", LONG);
            TYPESYSTEM_CACHE.put("float", FLOAT);
            TYPESYSTEM_CACHE.put("double", DOUBLE);
        }
    }

    public static NotchType getRuntimeType(Object rootVal) {
        if (rootVal instanceof HasCustomNotchType hasNotchType) {
            return hasNotchType.getNotchType();
        } else {
            Class<?> aClass = rootVal.getClass();
            return getType(aClass);
        }
    }

    public static NotchType getType(Class<?> aClass) {
        synchronized (TYPESYSTEM_CACHE) {
            return TYPESYSTEM_CACHE.computeIfAbsent(aClass.getName(), (name) -> newNotchTypeFor(aClass));
        }
    }

    public static NotchType getType(String className) {
        synchronized (TYPESYSTEM_CACHE) {
            return TYPESYSTEM_CACHE.computeIfAbsent(className, TypeSystem::newNotchTypeFor);
        }
    }

    private static NotchType newNotchTypeFor(Class aClass) {
        return new NotchJavaType(aClass);
    }

    private static NotchType newNotchTypeFor(String className) {
        try {
            Class<?> backingClass = Class.forName(className);
            return new NotchJavaType(backingClass);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
