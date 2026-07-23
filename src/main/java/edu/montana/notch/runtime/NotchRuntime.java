package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.statements.NotchStatement;
import edu.montana.notch.templates.runtime.RenderException;
import edu.montana.notch.types.NotchJavaType;
import edu.montana.notch.util.*;

import java.util.*;
import java.util.function.Consumer;

public class NotchRuntime {
    public static final Object UNDEFINED = new Object() {
        @Override
        public String toString() {
            return "<undefined>";
        }
    };

    public final Source source;
    private LinkedList<Scope> values = new LinkedList<>();
    protected BetterList<NotchStackTraceElement> stackTraceElements = new BetterList<>();
    private Consumer<Object> out = System.out::println;
    private BetterMap<Key, Object> storage = new BetterMap<>();

    public NotchRuntime(Source source) {
        this(source, Map.of());
    }

    public NotchRuntime(Source source, Map<String, Object> entryBlock) {
        this.source = Objects.requireNonNull(source);
        Objects.requireNonNull(entryBlock);
        var scope = new Scope(entryBlock);
        values.push(scope);

        final var currentStackTrace = Thread.currentThread().getStackTrace();
        final var frame = currentStackTrace[1];
        stackTraceElements.add(new NotchStackTraceElement(
                source.span,
                "<render@%s.%s:%d>".formatted(frame.getClassName(), frame.getMethodName(), frame.getLineNumber())
        ));
    }

    public NotchRuntime(Source source, NotchRuntime parent) {
        this.source = source;
        out = parent.out;
        values = new LinkedList<>(parent.values);
        stackTraceElements.addAll(parent.stackTraceElements);
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

    public void defineOrUpdateGlobal(String sym, Object value) {
        final var frame = values.get(0);
        frame.put(sym, value);
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

    public ScopeLock pushScope() {
        var entry = new Scope();
        values.push(entry);
        var lock = new ScopeLock(entry);
        return lock;
    }

    public TraceGuard trace(Spanned spanned, String hint) {
        var elt = new NotchStackTraceElement(spanned.span(), hint);
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

    public Iterable<?> coerceIterable(Span span, Object iterableValue) {

        if (iterableValue == null) {
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
        var diag = new Diagnostic();
        diag.setTitle("failed to coerce iterable from value");
        diag.highlight(span);
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
        var value = expression.evaluate(this);
        return value;
    }

    public void execute(NotchStatement stmt) {
        try {
            stmt.execute(this);
        } catch (NotchRuntimeException e) {
            throw Exceptions.rethrow(e);
        } catch (Throwable t) {
            var st = currentStackTrace();
            var diag = new Diagnostic();
            diag.setTitle("failed to execute statement");
            diag.highlight(stmt.span());
            diag.note("threw a %s".formatted(t.getClass().getName()));
            diag.note(t.getMessage());
            throw new NotchRuntimeException(st, diag, t);
        }
    }

    public <T> T storage(Key<T> key) {
        if (!storage.containsKey(key)) {
            throw new RenderException("key " + key + " not found in storage for " + source.id);
        }
        return (T) storage.get(key);
    }

    public <T> void storage(Key<T> key, T value) {
        storage.put(key, value);
    }

    public <T> void unstorage(Key<T> key) {
        storage.remove(key);
    }

    public static class Scope extends LinkedHashMap<String, Object> {
        public Scope() {
        }

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
