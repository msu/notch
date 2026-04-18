package bigsky.notch.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BetterMath {
    public static Number safeAdd(Number a, Number b) {
        NumberType typeA = getNumberType(a);
        NumberType typeB = getNumberType(b);
        NumberType resultType = getWiderType(typeA, typeB);

        return switch (resultType) {
            case BYTE -> {
                int result = a.intValue() + b.intValue();
                if (result < Byte.MIN_VALUE || result > Byte.MAX_VALUE) yield result;
                else yield (byte) result;
            }
            case SHORT -> {
                int result = a.intValue() + b.intValue();
                if (result < Short.MIN_VALUE || result > Short.MAX_VALUE) yield result;
                else yield (short) result;
            }
            case INTEGER -> {
                int av = a.intValue();
                int bv = b.intValue();try {
                    yield Math.addExact(av, bv);
                } catch (ArithmeticException ignored) {
                    yield (long) av + (long) bv;
                }
            }
            case LONG -> {
                long av = a.longValue();
                long bv = b.longValue();
                try {
                    yield Math.addExact(av, bv);
                } catch (ArithmeticException ignored) {
                    yield BigInteger.valueOf(av).add(BigInteger.valueOf(bv));
                }
            }
            case FLOAT, DOUBLE -> {
                var av = a.doubleValue();
                var bv = b.doubleValue();
                double result = av + bv;
                if (Double.isInfinite(result) && !Double.isInfinite(av) && !Double.isInfinite(bv)) {
                    yield BigDecimal.valueOf(av).add(BigDecimal.valueOf(bv));
                }
                yield result;
            }
            case BIG_INTEGER -> toBigInteger(a).add(toBigInteger(b));
            case BIG_DECIMAL -> toBigDecimal(a).add(toBigDecimal(b));
            default -> throw new IllegalArgumentException("Unsupported number type");
        };
    }

    public static Number safeSub(Number a, Number b) {
        NumberType typeA = getNumberType(a);
        NumberType typeB = getNumberType(b);
        NumberType resultType = getWiderType(typeA, typeB);

        return switch (resultType) {
            case BYTE -> {
                int result = a.intValue() - b.intValue();
                if (result < Byte.MIN_VALUE || result > Byte.MAX_VALUE) yield result;
                else yield (byte) result;
            }
            case SHORT -> {
                int result = a.intValue() - b.intValue();
                if (result < Short.MIN_VALUE || result > Short.MAX_VALUE) yield result;
                else yield (short) result;
            }
            case INTEGER -> {
                int av = a.intValue();
                int bv = b.intValue();
                try {
                    yield Math.subtractExact(av, bv);
                } catch (ArithmeticException ignored) {
                    yield (long) av - (long) bv;
                }
            }
            case LONG -> {
                long av = a.longValue();
                long bv = b.longValue();
                try {
                    yield Math.subtractExact(av, bv);
                } catch (ArithmeticException ignored) {
                    yield BigInteger.valueOf(av).subtract(BigInteger.valueOf(bv));
                }
            }
            case FLOAT, DOUBLE -> {
                var av = a.doubleValue();
                var bv = b.doubleValue();
                double result = av - bv;
                if (Double.isInfinite(result) && !Double.isInfinite(av) && !Double.isInfinite(bv)) {
                    yield BigDecimal.valueOf(av).subtract(BigDecimal.valueOf(bv));
                }
                yield result;
            }
            case BIG_INTEGER -> toBigInteger(a).subtract(toBigInteger(b));
            case BIG_DECIMAL -> toBigDecimal(a).subtract(toBigDecimal(b));
            default -> throw new IllegalArgumentException("Unsupported number type");
        };
    }

    public static Number safeMul(Number a, Number b) {
        NumberType typeA = getNumberType(a);
        NumberType typeB = getNumberType(b);
        NumberType resultType = getWiderType(typeA, typeB);

        return switch (resultType) {
            case BYTE -> {
                int result = a.intValue() * b.intValue();
                if (result < Byte.MIN_VALUE || result > Byte.MAX_VALUE) yield result;
                else yield (byte) result;
            }
            case SHORT -> {
                int result = a.intValue() * b.intValue();
                if (result < Short.MIN_VALUE || result > Short.MAX_VALUE) yield result;
                else yield (short) result;
            }
            case INTEGER -> {
                int av = a.intValue();
                int bv = b.intValue();
                try {
                    yield Math.multiplyExact(av, bv);
                } catch (ArithmeticException ignored) {
                    yield (long) av * (long) bv;
                }
            }
            case LONG -> {
                long av = a.longValue();
                long bv = b.longValue();
                try {
                    yield Math.multiplyExact(av, bv);
                } catch (ArithmeticException ignored) {
                    yield BigInteger.valueOf(av).multiply(BigInteger.valueOf(bv));
                }
            }
            case FLOAT, DOUBLE -> {
                var av = a.doubleValue();
                var bv = b.doubleValue();
                double result = av * bv;
                if (Double.isInfinite(result) && !Double.isInfinite(av) && !Double.isInfinite(bv)) {
                    yield BigDecimal.valueOf(av).multiply(BigDecimal.valueOf(bv));
                }
                yield result;
            }
            case BIG_INTEGER -> toBigInteger(a).multiply(toBigInteger(b));
            case BIG_DECIMAL -> toBigDecimal(a).multiply(toBigDecimal(b));
            default -> throw new IllegalArgumentException("Unsupported number type");
        };
    }

    public static Number safeDiv(Number a, Number b) {
        NumberType typeA = getNumberType(a);
        NumberType typeB = getNumberType(b);
        NumberType resultType = getWiderType(typeA, typeB);

        return switch (resultType) {
            case BYTE -> {
                int result = a.intValue() / b.intValue();
                if (result < Byte.MIN_VALUE || result > Byte.MAX_VALUE) yield result;
                else yield (byte) result;
            }
            case SHORT -> {
                int result = a.intValue() / b.intValue();
                if (result < Short.MIN_VALUE || result > Short.MAX_VALUE) yield result;
                else yield (short) result;
            }
            case INTEGER -> {
                int av = a.intValue();
                int bv = b.intValue();
                // SOURCE: I stole this from Java-18 divideExact
                int q = av / bv;
                if ((av & bv & q) >= 0) {
                    yield q;
                }
                yield (long) av / (long) bv;
            }
            case LONG -> {
                long av = a.longValue();
                long bv = b.longValue();
                // SOURCE: I stole this from Java-18 divideExact
                long q = av / bv;
                if ((av & bv & q) >= 0) {
                    yield q;
                }
                yield BigInteger.valueOf(av).divide(BigInteger.valueOf(bv));
            }
            case FLOAT, DOUBLE -> {
                var av = a.doubleValue();
                var bv = b.doubleValue();
                double result = av / bv;
                if (Double.isInfinite(result) && !Double.isInfinite(av) && !Double.isInfinite(bv)) {
                    yield BigDecimal.valueOf(av).divide(BigDecimal.valueOf(bv));
                }
                yield result;
            }
            case BIG_INTEGER -> toBigInteger(a).divide(toBigInteger(b));
            case BIG_DECIMAL -> toBigDecimal(a).divide(toBigDecimal(b));
            default -> throw new IllegalArgumentException("Unsupported number type");
        };
    }

    public static Number safeRem(Number a, Number b) {
        NumberType typeA = getNumberType(a);
        NumberType typeB = getNumberType(b);
        NumberType resultType = getWiderType(typeA, typeB);

        return switch (resultType) {
            case BYTE -> a.byteValue() % b.byteValue();
            case SHORT -> a.shortValue() % b.shortValue();
            case INTEGER -> a.intValue() % b.intValue();
            case LONG -> a.longValue() % b.longValue();
            case FLOAT, DOUBLE -> a.doubleValue() % b.doubleValue();
            case BIG_INTEGER -> toBigInteger(a).remainder(toBigInteger(b));
            case BIG_DECIMAL -> toBigDecimal(a).remainder(toBigDecimal(b));
            default -> throw new IllegalArgumentException("Unsupported number type");
        };
    }

    public static Number safeLShift(Number a, Number b) {
        NumberType typeA = getNumberType(a);
        int shiftAmount = b.intValue();

        // Shift only makes sense for integer types
        return switch (typeA) {
            case BYTE -> {
                int shifted = a.byteValue() << shiftAmount;
                if (shifted >= Byte.MIN_VALUE && shifted <= Byte.MAX_VALUE) {
                    yield (byte) shifted;
                } else {
                    yield shifted;
                }
            }
            case SHORT -> {
                int shifted = a.shortValue() << shiftAmount;
                if (shifted >= Short.MIN_VALUE && shifted <= Short.MAX_VALUE) {
                    yield (short) shifted;
                } else {
                    yield shifted;
                }
            }
            case INTEGER -> {
                try {
                    yield Math.multiplyExact(a.intValue(), 1 << shiftAmount);
                } catch (ArithmeticException ignored) {
                    yield (long) a.intValue() << shiftAmount;
                }
            }
            case LONG -> {
                long av = a.longValue();
                if (shiftAmount >= 0 && shiftAmount < 64) {
                    // Check for overflow
                    if (av > 0 && av > (Long.MAX_VALUE >> shiftAmount)) {
                        yield toBigInteger(a).shiftLeft(shiftAmount);
                    } else {
                        yield av << shiftAmount;
                    }
                } else {
                    yield toBigInteger(a).shiftLeft(shiftAmount);
                }
            }
            case BIG_INTEGER -> toBigInteger(a).shiftLeft(shiftAmount);
            default -> throw new IllegalArgumentException("Left shift not supported for type: " + typeA);
        };
    }
    public static Number safeRShift(Number a, Number b) {
        NumberType typeA = getNumberType(a);
        int shiftAmount = b.intValue();

        // Shift only makes sense for integer types
        return switch (typeA) {
            case BYTE -> (byte)(a.byteValue() >> shiftAmount);
            case SHORT -> (short)(a.shortValue() >> shiftAmount);
            case INTEGER -> a.intValue() >> shiftAmount;
            case LONG -> a.longValue() >> shiftAmount;
            case BIG_INTEGER -> toBigInteger(a).shiftRight(shiftAmount);
            default -> throw new IllegalArgumentException("Right shift not supported for type: " + typeA);
        };
    }

    public enum Ordering {
        LESS_THAN,
        EQUAL,
        GREATER_THAN,
    }

    public static Ordering safeCompare(Number a, Number b) {
        NumberType typeA = getNumberType(a);
        NumberType typeB = getNumberType(b);
        NumberType resultType = getWiderType(typeA, typeB);

        return switch (resultType) {
            case BYTE, SHORT, INTEGER, LONG -> {
                long av = a.longValue();
                long bv = b.longValue();
                if (av < bv) yield Ordering.LESS_THAN;
                else if (av > bv) yield Ordering.GREATER_THAN;
                yield Ordering.EQUAL;
            }
            case FLOAT, DOUBLE -> {
                var av = a.doubleValue();
                var bv = b.doubleValue();
                if (av < bv) yield Ordering.LESS_THAN;
                else if (av > bv) yield Ordering.GREATER_THAN;
                yield Ordering.EQUAL;
            }
            case BIG_INTEGER -> {
                var av = toBigInteger(a);
                var bv = toBigInteger(b);
                if (av.compareTo(bv) < 0) yield Ordering.LESS_THAN;
                if (av.compareTo(bv) > 0) yield Ordering.GREATER_THAN;
                yield Ordering.EQUAL;
            }
            case BIG_DECIMAL -> {
                var av = toBigDecimal(a);
                var bv = toBigDecimal(b);
                if (av.compareTo(bv) < 0) yield Ordering.LESS_THAN;
                if (av.compareTo(bv) > 0) yield Ordering.GREATER_THAN;
                yield Ordering.EQUAL;
            }
            default -> throw new IllegalArgumentException("Unsupported number type");
        };
    }

    private enum NumberType {
        BYTE(1),
        SHORT(2),
        INTEGER(3),
        LONG(4),
        FLOAT(5),
        DOUBLE(6),
        BIG_INTEGER(7),
        BIG_DECIMAL(8);

        private final int priority;

        NumberType(int priority) {
            this.priority = priority;
        }

        public NumberType normalized() {
            return switch (this) {
                case BYTE, SHORT, INTEGER -> INTEGER;
                case LONG -> LONG;
                case FLOAT, DOUBLE -> DOUBLE;
                case BIG_INTEGER -> BIG_INTEGER;
                case BIG_DECIMAL -> BIG_DECIMAL;
            };
        }
    }

    private static NumberType getNumberType(Number n) {
        if (n instanceof Byte) return NumberType.BYTE;
        if (n instanceof Short) return NumberType.SHORT;
        if (n instanceof Integer) return NumberType.INTEGER;
        if (n instanceof Long) return NumberType.LONG;
        if (n instanceof Float) return NumberType.FLOAT;
        if (n instanceof Double) return NumberType.DOUBLE;
        if (n instanceof BigInteger) return NumberType.BIG_INTEGER;
        if (n instanceof BigDecimal) return NumberType.BIG_DECIMAL;

        // For custom Number implementations, default to BigDecimal for safety
        return NumberType.BIG_DECIMAL;
    }

    private static NumberType getWiderType(NumberType a, NumberType b) {
        var na = a.normalized();
        var nb = b.normalized();
        if (na == NumberType.DOUBLE) {
            if (nb == NumberType.BIG_DECIMAL) return NumberType.BIG_DECIMAL;
            if (nb == NumberType.BIG_INTEGER) return NumberType.BIG_INTEGER;
            return NumberType.DOUBLE;
        } else if (nb == NumberType.DOUBLE) {
            if (na == NumberType.BIG_DECIMAL) return NumberType.BIG_DECIMAL;
            if (na == NumberType.BIG_INTEGER) return NumberType.BIG_INTEGER;
            return NumberType.DOUBLE;
        } else if (a.priority >= b.priority) {
            return a;
        } else {
            return b;
        }
    }

    private static boolean isDecimalType(NumberType type) {
        return type == NumberType.FLOAT || type == NumberType.DOUBLE || type == NumberType.BIG_DECIMAL;
    }

    private static boolean isIntegerType(NumberType type) {
        return type == NumberType.BYTE || type == NumberType.SHORT || type == NumberType.INTEGER || type == NumberType.LONG || type == NumberType.BIG_INTEGER;
    }

    private static BigInteger toBigInteger(Number n) {
        if (n instanceof BigInteger i) return i;
        if (n instanceof BigDecimal d) return d.toBigInteger();
        return BigInteger.valueOf(n.longValue());
    }

    private static BigDecimal toBigDecimal(Number n) {
        if (n instanceof BigDecimal d) return d;
        if (n instanceof BigInteger i) return new BigDecimal(i);
        if (n instanceof Float || n instanceof Double) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.valueOf(n.longValue());
    }

    public static int align(int n, int to) {
        return to * ((n + to - 1) / to);
    }
}
