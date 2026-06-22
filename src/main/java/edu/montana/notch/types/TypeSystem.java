package edu.montana.notch.types;

import java.util.concurrent.ConcurrentHashMap;

public class TypeSystem {
    public static final NotchJavaType BOOLEAN = new NotchJavaType(Boolean.TYPE);
    public static final NotchJavaType BYTE = new NotchJavaType(Byte.TYPE);
    public static final NotchJavaType CHAR = new NotchJavaType(Character.TYPE);
    public static final NotchJavaType SHORT = new NotchJavaType(Short.TYPE);
    public static final NotchJavaType INT = new NotchJavaType(Integer.TYPE);
    public static final NotchJavaType LONG = new NotchJavaType(Long.TYPE);
    public static final NotchJavaType FLOAT = new NotchJavaType(Float.TYPE);
    public static final NotchJavaType DOUBLE = new NotchJavaType(Double.TYPE);

    private static final ConcurrentHashMap<String, NotchType> TYPESYSTEM_CACHE = new ConcurrentHashMap<>();
    private static ClassLoader contextClassLoader;

    static {
        synchronized (TYPESYSTEM_CACHE) {
            init();
            contextClassLoader = TypeSystem.class.getClassLoader();
        }
    }

    public static void refresh(ClassLoader classLoader) {
        contextClassLoader = classLoader;
        init();
    }

    private static void init() {
        // put primitives into type system by name because they do not resolve normally
        TYPESYSTEM_CACHE.clear();
        TYPESYSTEM_CACHE.put("boolean", BOOLEAN);
        TYPESYSTEM_CACHE.put("byte", BYTE);
        TYPESYSTEM_CACHE.put("char", CHAR);
        TYPESYSTEM_CACHE.put("short", SHORT);
        TYPESYSTEM_CACHE.put("int", INT);
        TYPESYSTEM_CACHE.put("long", LONG);
        TYPESYSTEM_CACHE.put("float", FLOAT);
        TYPESYSTEM_CACHE.put("double", DOUBLE);
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

    public static Class<?> resolveThrowableType(String name) {
        //allows user to call catch IOException, without implementing parallel exception structure that java provides.
        NotchType direct = getType(name);
        if (direct != null) return direct.getBackingClass();
        //if user enters simple name exception not in scope
        if (name.indexOf('.') >= 0) return null;
        for (String pkg : DEFAULT_THROWABLE_PACKAGES) {
            NotchType type = getType(pkg + "." + name);
            if (type != null) {
                TYPESYSTEM_CACHE.put(name, type);
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
