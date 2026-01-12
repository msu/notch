package bigsky.notch.runtime;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.statements.NotchStatement;
import bigsky.notch.types.NotchJavaType;
import bigsky.utils.BetterList;
import bigsky.utils.Exceptions;
import bigsky.utils.SafeAutoClosable;
import bigsky.utils.chisel.Span;

import java.util.*;
import java.util.function.Consumer;

public class NotchRuntime {
    public static final Object UNDEFINED = new Object() {
        @Override
        public String toString() {
            return "<undefined>";
        }
    };

    public final String fileId;
    private LinkedList<Scope> values = new LinkedList<>();
    protected BetterList<NotchStackTraceElement> stackTraceElements = new BetterList<>();
    private Consumer<Object> out = System.out::println;

    public NotchRuntime(String fileId) {
        this(fileId, Map.of());
    }

    public NotchRuntime(String fileId, Map<String, Object> entryBlock) {
        this.fileId = Objects.requireNonNull(fileId);
        Objects.requireNonNull(entryBlock);
        var scope = new Scope(entryBlock);
        values.push(scope);
        var trace = new NotchStackTraceElement(fileId, Span.CALLSITE, "<render>");
        stackTraceElements.add(trace);
    }

    public NotchRuntime(String fileId, NotchRuntime parent) {
        this.fileId = Objects.requireNonNull(fileId);
        out = parent.out;
        values = new LinkedList<>(parent.values);
        stackTraceElements.addAll(parent.stackTraceElements);
        var trace = new NotchStackTraceElement(fileId, Span.CALLSITE, "<render>");
        stackTraceElements.add(trace);
    }

    public static String className(Object obj) {
        if (obj == null) {
            return "<null>";
        } else if (obj == UNDEFINED) {
            return "<undefined>";
        }
        return obj.getClass().getName();
    }

    public Object getSymbol(String sym) {
        for (var frame : values) {
            if (frame.containsKey(sym)) {
                return frame.get(sym);
            }
        }
        return UNDEFINED;
    }

    public void unsetSymbol(String sym) {
        for (var frame : values) {
            if (frame.containsKey(sym)) {
                frame.remove(sym);
                break;
            }
        }
    }

    public Set<String> getInScopeSymbols() {
        Set<String> symbols = new LinkedHashSet<>();
        for (var frame : values) {
            symbols.addAll(frame.keySet());
        }
        return symbols;
    }

    public void defineOrUpdate(String sym, Object value) {
        if (isUndefined(value)) {
            unsetSymbol(sym);
        }

        for (var frame : values) {
            if (frame.containsKey(sym)) {
                frame.put(sym, value);
            }
        }

        var frame = values.peek();
        frame.put(sym, value);
    }

    public ScopeLock pushScope(String fileId, Span span) {
        var entry = new Scope();
        values.push(entry);
        var lock = new ScopeLock(entry);
        return lock;
    }

    public TraceGuard trace(String fileId, Span span, String hint) {
        var elt = new NotchStackTraceElement(fileId, span, hint);
        return new TraceGuard(elt);
    }

    public void setOut(Consumer<Object> out) {
        this.out = out;
    }

    public void println(Object result) {
        out.accept(result);
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

    public Iterable<?> coerceIterable(String fileId, Span span, Object iterableValue) {

        if(iterableValue == null) {
            return Collections.emptyList();
        }

        if (iterableValue instanceof Object[] oa) {
            return List.of(oa);
        }

        if (iterableValue instanceof Iterable<?> it) {
            return it;
        }

        // convert notch java types to classes so enums are iterable
        if (iterableValue instanceof NotchJavaType t) {
            iterableValue = t.getBackingClass();
        }

        if (iterableValue instanceof Class<?> c && c.isEnum()) {
            return Arrays.asList(c.getEnumConstants());
        }

        var st = currentStackTrace();
        var diag = new NotchDiagnostic();
        diag.setTitle("failed to coerce iterable from value");
        diag.highlight(fileId, span);
        diag.note("target class was " + className(iterableValue));
        throw new NotchRuntimeException(st, diag);
    }

    public NotchStackTrace currentStackTrace() {
        var trace = stackTraceElements.toArray(NotchStackTraceElement[]::new);
        return new NotchStackTrace(trace);
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
            var f = values.pop();
            if (f != frame) {
                throw new IllegalStateException("popped different scope than was pushed!");
            }
        }
    }

    public Object evaluate(NotchExpression expression) {
        try {
            var value = expression.evaluate(this);
            return value;
        } catch (NotchRuntimeException e) {
            throw Exceptions.rethrow(e);
        } catch (Throwable t) {
            var st = currentStackTrace();
            var diag = new NotchDiagnostic();
            diag.setTitle("failed to evaluate expression");
            diag.highlight(expression.fileId, expression.span());
            diag.note(t.getMessage());
            throw new NotchRuntimeException(st, diag, t);
        }
    }

    public void execute(NotchStatement stmt) {
        try {
            stmt.execute(this);
        } catch (NotchRuntimeException e) {
            throw Exceptions.rethrow(e);
        } catch (Throwable t) {
            var st = currentStackTrace();
            var diag = new NotchDiagnostic();
            diag.setTitle("failed to execute statement");
            diag.highlight(stmt.fileId, stmt.span());
            diag.note(t.getMessage());
            throw new NotchRuntimeException(st, diag, t);
        }
    }

    public static class Scope extends LinkedHashMap<String, Object> {
        public Scope() {}

        public Scope(Map<? extends String, ?> m) {
            super(m);
        }
    }

    public class TraceGuard implements SafeAutoClosable {
        public final NotchStackTraceElement element;

        public TraceGuard(NotchStackTraceElement element) {
            this.element = Objects.requireNonNull(element);
            stackTraceElements.add(this.element);
        }

        @Override
        public void close() {
            var removed = stackTraceElements.removeLast();
            assert element == removed;
        }
    }
}
