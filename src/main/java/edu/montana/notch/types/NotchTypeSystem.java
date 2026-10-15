package edu.montana.notch.types;

import edu.montana.notch.util.BetterList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;

public class NotchTypeSystem {
    private static final ConcurrentHashMap<String, NotchType> TYPESYSTEM_CACHE = new ConcurrentHashMap<>();
    private static ClassLoader contextClassLoader;

    static {
        synchronized (TYPESYSTEM_CACHE) {
            init();
            contextClassLoader = NotchTypeSystem.class.getClassLoader();
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
        TYPESYSTEM_CACHE.put("void", new NotchJavaType(Void.TYPE));
        TYPESYSTEM_CACHE.put("undefined", NotchUndefinedType.INST);
    }

    public static NotchType undefined() {
        return NotchUndefinedType.INST;
    }

    public static NotchType getRuntimeType(Object rootVal) {
        if (rootVal == null) return getType(Void.TYPE);
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

    private static final String[] DEFAULT_PACKAGES = {"java.lang", "java.io", "java.util"};

    /**
     * Resolves a type name as written in source. A name with no {@code .} that is not a class or
     * primitive is also tried in {@link #DEFAULT_PACKAGES}, so {@code String} and {@code IOException}
     * work without an import. Returns {@code null} if nothing matches.
     */
    public static NotchType resolveTypeName(String name) {
        NotchType direct = getType(name);
        if (direct != null || name.indexOf('.') >= 0) return direct;
        for (String pkg : DEFAULT_PACKAGES) {
            NotchType type = getType(pkg + "." + name);
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    public static Class<?> resolveThrowableType(String name) {
        NotchType type = resolveTypeName(name);
        return type == null ? null : type.getBackingClass();
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

    public static BetterList<NotchType> getRuntimeTypes(Object[] args) {
        var out = new BetterList<NotchType>(new ArrayList<>(args.length));
        for (Object arg : args) {
            out.add(getRuntimeType(arg));
        }
        return out;
    }

    public static BetterList<NotchType> getRuntimeTypes(List<Object> args) {
        var out = new BetterList<NotchType>(new ArrayList<>(args.size()));
        for (Object arg : args) {
            out.add(getRuntimeType(arg));
        }
        return out;
    }
}
