package bigsky.notch;

import java.util.*;

public class NotchRuntime {
    public static final Object UNDEFINED = new Object();

    private final Vector<LinkedHashMap<String, Object>> values = new Vector<>();

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

    public void write(String sym, Object value) {
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

    public class ScopeLock implements AutoCloseable {
        private final LinkedHashMap<String, Object> frame;

        private ScopeLock(LinkedHashMap<String, Object> frame) {
            this.frame = frame;
        }

        @Override
        public void close() {
            var f = values.removeLast();
            if (f != frame) {
                throw new IllegalStateException("popped different scope than was pushed!");
            }
        }
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
}
