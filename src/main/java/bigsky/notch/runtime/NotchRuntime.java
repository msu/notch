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

    private LinkedList<Scope> values = new LinkedList<>();
    private final BetterList<NotchStackTraceElement> stackTraceElements = new BetterList<>();
    private Consumer<Object> out = System.out::println;

    public NotchRuntime(String fileId, Span span) {
        var first = new Scope();
        values.push(first);
        var trace = new NotchStackTraceElement(fileId, span);
        stackTraceElements.add(trace);
    }

    public NotchRuntime(String fileId) {
        this(fileId, Map.of());
    }

    public NotchRuntime(String fileId, Map<String, Object> entryBlock) {
        Objects.requireNonNull(entryBlock);
        var first = new Scope(entryBlock);
        values.push(first);
        var trace = new NotchStackTraceElement(fileId, Span.CALLSITE);
        stackTraceElements.add(trace);
    }

    public NotchRuntime(NotchRuntime parent) {
        out = parent.out;
        values = new LinkedList<>(parent.values);
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

    public void setOut(Consumer<Object> out) {
        this.out = out;
    }

    public void println(Object result) {
        out.accept(result);
    }

    public NotchRuntime captureClosure(String fileId, Span span) {
        NotchRuntime closure = new NotchRuntime(fileId, span);
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
        diag.note("target class was " + iterableValue.getClass().getName());
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
        NotchStackTraceElement ste;
        try {
            ste = new NotchStackTraceElement(stmt.fileId, stmt.span());
            stackTraceElements.add(ste);
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
        } finally {
            stackTraceElements.removeLast();
        }
    }

    public static class Scope extends LinkedHashMap<String, Object> {
        public Scope() {}

        public Scope(Map<? extends String, ?> m) {
            super(m);
        }
    }

}
