package bigsky.notch.runtime;

import bigsky.utils.Numbers;
import bigsky.utils.SafeAutoClosable;
import bigsky.utils.chisel.Span;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

public class NotchRuntime {
    public static final Object UNDEFINED = new Object() {
        @Override
        public String toString() {
            return "<undefined>";
        }
    };

    private LinkedList<LinkedHashMap<String, Object>> values = new LinkedList<>();

    Consumer<Object> out = System.out::println;

    public NotchRuntime() {
        this(Map.of());
    }

    public NotchRuntime(Map<String, Object> entry) {
        Objects.requireNonNull(entry);

        var first = new LinkedHashMap<>(entry);
        values.addLast(first);
    }

    public Object getSymbol(String sym) {
        for (var frame : values.reversed()) {
            if (frame.containsKey(sym)) {
                return frame.get(sym);
            }
        }
        return UNDEFINED;
    }

    public void unsetSymbol(String sym) {
        for (var frame : values.reversed()) {
            if (frame.containsKey(sym)) {
                frame.remove(sym);
                break;
            }
        }
    }

    public void setOrInsert(String sym, Object value) {
        if (isUndefined(value)) {
            unsetSymbol(sym);
        }

        for (var frame : values.reversed()) {
            if (frame.containsKey(sym)) {
                frame.put(sym, value);
            }
        }

        var frame = values.getLast();
        frame.put(sym, value);
    }

    public ScopeLock pushScope() {
        var entry = new LinkedHashMap<String, Object>();
        values.addLast(entry);
        var lock = new ScopeLock(entry);
        return lock;
    }

    public void setOut(Consumer<Object> out) {
        this.out = out;
    }

    public void println(Object result) {
        out.accept(result);
    }

    public NotchRuntime captureClosure() {
        NotchRuntime closure = new NotchRuntime();
        closure.values = new LinkedList<>(values);
        return closure;
    }

    public boolean isUndefined(Object value) {
        return UNDEFINED == value;
    }

    public boolean isTruthy(Object value) {
        return Objects.equals(true, value);
    }

    public boolean isFalsy(Object value) {
        return !isTruthy(value);
    }

    public Iterable<?> asIterable(Span span, Object iterableValue) {
        if (iterableValue instanceof Object[] oa) {
            return List.of(oa);
        }

        if (iterableValue instanceof Iterable<?> it) {
            return it;
        }

        throw new NotchRuntimeException(span, "conversion error, cannot convert " + iterableValue.getClass() + " as an iterable");
    }

    public Number asNumber(Object value) {
        if (value instanceof Number n) return n;
        return 0;
    }

    public class ScopeLock implements SafeAutoClosable {
        private final LinkedHashMap<String, Object> frame;

        private ScopeLock(LinkedHashMap<String, Object> frame) {
            this.frame = frame;
        }

        public void define(String name, Object value) {
            this.frame.put(name, value);
        }

        public Object getSymbol(String name) {
            if (frame.containsKey(name)) {
                return frame.get(name);
            }
            return UNDEFINED;
        }

        @Override
        public void close() {
            var f = values.removeLast();
            if (f != frame) {
                throw new IllegalStateException("popped different scope than was pushed!");
            }
        }
    }
}
