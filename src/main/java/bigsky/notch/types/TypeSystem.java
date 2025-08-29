package bigsky.notch.types;

import java.util.concurrent.ConcurrentHashMap;

public class TypeSystem {

    private static final ConcurrentHashMap<String, NotchType> TYPESYSTEM_CACHE = new ConcurrentHashMap<>();

    public static NotchType getRuntimeType(Object rootVal) {
        if (rootVal instanceof HasCustomNotchType) {
            HasCustomNotchType hasChillType = (HasCustomNotchType) rootVal;
            return hasChillType.getChillType();
        } else {
            Class<?> aClass = rootVal.getClass();
            return getType(aClass);
        }
    }

    public static NotchType getType(Class<?> aClass) {
        return TYPESYSTEM_CACHE.computeIfAbsent(aClass.getSimpleName(), (name) -> getRuntimeTypeNoCache(aClass));
    }

    private static NotchType getRuntimeTypeNoCache(Class aClass) {
        return new NotchJavaType(aClass);
    }
}
