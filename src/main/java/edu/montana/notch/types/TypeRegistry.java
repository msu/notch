package edu.montana.notch.types;

import java.util.concurrent.ConcurrentHashMap;

public class TypeRegistry {
    private static final ConcurrentHashMap<String, NotchType> TYPESYSTEM_CACHE = new ConcurrentHashMap<>();
    private static ClassLoader contextClassLoader;

    static {
        synchronized (TYPESYSTEM_CACHE) {
            init();
            contextClassLoader = TypeRegistry.class.getClassLoader();
        }
    }

    public static void refresh(ClassLoader classLoader) {
        contextClassLoader = classLoader;
        init();
    }

    private static void init() {
        // put primitives into type system by name because they do not resolve normally
        TYPESYSTEM_CACHE.clear();
        TYPESYSTEM_CACHE.put("boolean", new NotchJavaType(Boolean.TYPE));
        TYPESYSTEM_CACHE.put("byte", new NotchJavaType(Byte.TYPE));
        TYPESYSTEM_CACHE.put("char", new NotchJavaType(Character.TYPE));
        TYPESYSTEM_CACHE.put("short", new NotchJavaType(Short.TYPE));
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
        return TYPESYSTEM_CACHE.computeIfAbsent(aClass.getName(), (str) -> {
            return new NotchJavaType(aClass);
        });
    }

    private static final String[] DEFAULT_THROWABLE_PACKAGES = {"java.lang", "java.io", "java.util"};

    //asymmetry for Notch allowing "catch IOException" without import
    public static Class<?> resolveThrowableType(String name) {
        NotchType direct = getType(name);
        if (direct != null) return direct.getBackingClass();
        if (name.indexOf('.') >= 0) return null;
        for (String pkg : DEFAULT_THROWABLE_PACKAGES) {
            NotchType type = getType(pkg + "." + name);
            if (type != null) {
                return type.getBackingClass();
            }
        }
        return null;
    }

    public static NotchType getType(String className) {
        NotchType cached = TYPESYSTEM_CACHE.get(className);
        if (cached != null) {
            return cached;
        }
        try {
            Class<?> backingClass = Class.forName(className, true, contextClassLoader);
            return getType(backingClass);
        } catch (ClassNotFoundException e) {
            return switch (className) {
                case "int" -> getType(int.class);
                case "char" -> getType(char.class);
                case "float" -> getType(float.class);
                case "double" -> getType(double.class);
                case "boolean" -> getType(boolean.class);
                case "long" -> getType(long.class);
                case "short" -> getType(short.class);
                case "byte" -> getType(byte.class);
                default -> null;
            };
        }
    }
}
