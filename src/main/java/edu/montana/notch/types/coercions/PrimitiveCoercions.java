package edu.montana.notch.types.coercions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.NotchTypeCoercer;
import edu.montana.notch.util.Pair;

import java.util.HashMap;

import static edu.montana.notch.util.Pair.pair;

public class PrimitiveCoercions implements NotchTypeCoercer {
    public static final PrimitiveCoercions INSTANCE = new PrimitiveCoercions();

    private final HashMap<Pair<Class, Class>, Integer> coercionCost = new HashMap<>();
    private PrimitiveCoercions() {
        Pair<Class, Class> CHARACTER = pair(Character.TYPE, Character.class);
        Pair<Class, Class> BYTE = pair(Byte.TYPE, Byte.class);
        Pair<Class, Class> SHORT = pair(Short.TYPE, Short.class);
        Pair<Class, Class> INTEGER = pair(Integer.TYPE, Integer.class);
        Pair<Class, Class> LONG = pair(Long.TYPE, Long.class);
        Pair<Class, Class> DOUBLE = pair(Double.TYPE, Double.class);
        Pair<Class, Class> FLOAT = pair(Float.TYPE, Float.class);
        Pair<Class, Class> BOOLEAN = pair(Boolean.TYPE, Boolean.class);
        Pair<Class, Class>[] primitiveBoxPairs = new Pair[] {BYTE, SHORT, INTEGER, LONG, DOUBLE, FLOAT, BOOLEAN, CHARACTER};
        Pair<Class, Class>[] ints = new Pair[] {BYTE, SHORT, INTEGER, LONG};
        Pair<Class, Class>[] decimals = new Pair[] {FLOAT, DOUBLE};

        // primitive <-> box : cost 1
        // int <-> int : cost 2/3
        // int <-> decimal : cost 3/4
        // decimal -> decimal : cost 2 / 3
        // int <-> char : cost 4/5

        for (var pb : primitiveBoxPairs) {
            coercionCost.put(pb, 1);
            coercionCost.put(pair(pb.second(), pb.first()), 1);
        }
        for (var t : ints) {
            for (var t2 : ints) {
                if (t == t2) continue;
                coercionCost.put(pair(t.first(), t2.first()), 2);
                coercionCost.put(pair(t.first(), t2.second()), 3);
                coercionCost.put(pair(t.second(), t2.first()), 3);
                coercionCost.put(pair(t.second(), t2.second()), 2);
            }
            for (var t2 : decimals) {
                coercionCost.put(pair(t.first(), t2.first()), 3);
                coercionCost.put(pair(t.first(), t2.second()), 4);
                coercionCost.put(pair(t.second(), t2.first()), 4);
                coercionCost.put(pair(t.second(), t2.second()), 3);
            }
            int charBase = (t.first() == int.class || t.first() == byte.class) ? 3 : 4;
            coercionCost.put(pair(t.first(), Character.TYPE), charBase);
            coercionCost.put(pair(t.first(), Character.class), charBase + 1);
            coercionCost.put(pair(t.second(), Character.TYPE), charBase + 1);
            coercionCost.put(pair(t.second(), Character.class), charBase);
        }
        for (var t : decimals) {
            for (var t2 : decimals) {
                if (t == t2) continue;
                coercionCost.put(pair(t.first(), t2.first()), 2);
                coercionCost.put(pair(t.first(), t2.second()), 3);
                coercionCost.put(pair(t.second(), t2.first()), 3);
                coercionCost.put(pair(t.second(), t2.second()), 2);
            }
        }

    }

    private static boolean isPrimitiveNum(Class c) {
        if (c == int.class) return true;
        if (c == long.class) return true;
        if (c == double.class) return true;
        if (c == float.class) return true;
        if (c == short.class) return true;
        if (c == byte.class) return true;
        return false;
    }

    @Override
    public int getCoercionCost(NotchType source, NotchType target) {
        Class sourceClass = source.getBackingClass();
        Class targetClass = target.getBackingClass();
        if (sourceClass == null || targetClass == null) {
            return Integer.MAX_VALUE;
        }

        var typePair = pair(sourceClass, targetClass);
        if (coercionCost.containsKey(typePair)) return coercionCost.get(typePair);

        if (Number.class.isAssignableFrom(targetClass)) {
            if (sourceClass.isPrimitive() && isPrimitiveNum(sourceClass)) return 4;
            else if (Number.class.isAssignableFrom(sourceClass)) return 3;
        }

        if (isPrimitiveNum(targetClass) && Number.class.isAssignableFrom(sourceClass)) return 4;

        return Integer.MAX_VALUE;
    }

    @Override
    public Object coerce(NotchRuntime runtime, Object value, Span span, NotchType source, NotchType target) {
        Class sourceClass = source.getBackingClass();
        Class targetClass = target.getBackingClass();
        if (sourceClass == null || targetClass == null) {
            throw new CoercionFailure();
        }

        if (targetClass == boolean.class || targetClass == Boolean.class)
            return Boolean.class.cast(value);

        final var n = Number.class.cast(value);
        if (targetClass == int.class || targetClass == Integer.class) {
            return n.intValue();
        } else if (targetClass == long.class || targetClass == Long.class) {
            return n.longValue();
        } else if (targetClass == double.class || targetClass == Double.class) {
            return n.doubleValue();
        } else if (targetClass == float.class || targetClass == Float.class) {
            return n.floatValue();
        } else if (targetClass == short.class || targetClass == Short.class) {
            return n.shortValue();
        } else if (targetClass == byte.class || targetClass == Byte.class) {
            return n.byteValue();
        } else if (targetClass == Character.TYPE || targetClass == Character.class) {
            return (char) n.intValue();
        }

        throw new CoercionFailure();
    }
}
