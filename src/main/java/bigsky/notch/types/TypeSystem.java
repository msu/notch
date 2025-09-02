package bigsky.notch.types;

import java.util.concurrent.ConcurrentHashMap;

public class TypeSystem {

    private static final ConcurrentHashMap<String, NotchType> TYPESYSTEM_CACHE = new ConcurrentHashMap<>();
    static {
        // put primitives into type system by name because they do not resolve normally
        TYPESYSTEM_CACHE.put("boolean", new NotchJavaType(Boolean.TYPE));
        TYPESYSTEM_CACHE.put("byte", new NotchJavaType(Byte.TYPE));
        TYPESYSTEM_CACHE.put("char", new NotchJavaType(Character.TYPE));
        TYPESYSTEM_CACHE.put("int", new NotchJavaType(Integer.TYPE));
        TYPESYSTEM_CACHE.put("long", new NotchJavaType(Long.TYPE));
        TYPESYSTEM_CACHE.put("float", new NotchJavaType(Float.TYPE));
        TYPESYSTEM_CACHE.put("double", new NotchJavaType(Double.TYPE));
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
        return TYPESYSTEM_CACHE.computeIfAbsent(aClass.getName(), (name) -> newNotchTypeFor(aClass));
    }

    public static NotchType getType(String className) {
        return TYPESYSTEM_CACHE.computeIfAbsent(className, TypeSystem::newNotchTypeFor);
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
