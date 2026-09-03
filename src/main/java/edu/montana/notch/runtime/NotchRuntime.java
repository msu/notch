package edu.montana.notch.runtime;

import edu.montana.notch.Notch;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.statements.NotchStatement;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeRegistry;
import edu.montana.notch.types.coercions.Coercion;
import edu.montana.notch.types.coercions.CoercionGraph;
import edu.montana.notch.types.coercions.CoercionPath;
import edu.montana.notch.util.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NotchRuntime {
    public static final Object UNDEFINED = new Object() {
        @Override
        public String toString() {
            return "<undefined>";
        }
    };

    public final Source source;
    private LinkedList<Scope> values = new LinkedList<>();
    protected BetterList<NotchStackTraceElement> stackTrace = new BetterList<>();
    protected CoercionGraph coercions = new CoercionGraph();
    // TODO: convert out to be in storage
    private Consumer<Object> out = System.out::print;
    private BetterMap<Key, Object> storage = new BetterMap<>();
    private final Deque<Throwable> handledExceptions = new ArrayDeque<>();

    public static final NotchRuntime BUILTIN = new NotchRuntime(new Source("notch.builtins", "._."), Map.of());
    public NotchRuntime(Source source) {
        this(source, Notch.builtins());
    }

    public NotchRuntime(Source source, Map<String, Object> entryBlock) {
        this.source = Objects.requireNonNull(source);
        Objects.requireNonNull(entryBlock);
        var scope = new Scope(entryBlock);
        values.push(scope);

        coercions.addAll(Notch.coercions());


        final var currentStackTrace = Thread.currentThread().getStackTrace();
        final var frame = currentStackTrace[1];
        stackTrace.add(new NotchStackTraceElement(
                source.span,
                "<render@%s.%s:%d>".formatted(frame.getClassName(), frame.getMethodName(), frame.getLineNumber())
        ));
    }

    public NotchRuntime(Source source, NotchRuntime parent) {
        this.source = source;
        out = parent.out;
        values = new LinkedList<>(parent.values);
        stackTrace.addAll(parent.stackTrace);
        coercions = new CoercionGraph(parent.coercions);
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

    public void print(Object result) {
        out.accept(result);
    }

    public void println(Object result) {
        out.accept(result);
        out.accept('\n');
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

    public NotchStackTrace currentStackTrace() {
        var trace = stackTrace.toArray(NotchStackTraceElement[]::new);
        return new NotchStackTrace(trace);
    }

    public void pushHandled(Throwable t) {
        handledExceptions.push(t);
    }

    public void popHandled() {
        handledExceptions.pop();
    }

    public Throwable currentHandled() {
        return handledExceptions.peek();
    }

    public NotchType typeOf(Class<?> clazz) {
        return TypeRegistry.getType(clazz);
    }

    public NotchType typeOf(Object value) {
        return TypeRegistry.getRuntimeType(value);
    }

    @SuppressWarnings("unchecked")
    public <T> T coerce(Class<T> clazz, Object value, Spanned item) {
        if (value == null || isUndefined(value) || clazz.isInstance(value)) {
            return cast(clazz, value, item);
        }
        var path = getCoercion(typeOf(value), clazz);
        if (path == null) return cast(clazz, value, item);
        return (T) path.apply(value);
    }

    public void addCoercion(Coercion coercion) {
        coercions.add(coercion);
    }

    public CoercionPath getCoercion(Class<?> from, Class<?> to) {
        return coercions.find(typeOf(from), to);
    }

    public CoercionPath getCoercion(NotchType from, Class<?> to) {
        return coercions.find(from, to);
    }

    public <T> T cast(Class<T> clazz, Object value, Spanned item) {
        try {
            return clazz.cast(value);
        } catch (ClassCastException e) {
            String valueType;
            if (value == null) valueType = "null";
            else if (isUndefined(value)) valueType = "undefined";
            else valueType = TypeRegistry.getRuntimeType(value).getDisplayName();
            var targetType = TypeRegistry.getType(clazz).getDisplayName();

            var diag = new Diagnostic();
            diag.setTitle("value cast failed");
            diag.highlight(item);
            diag.note("unable to cast %s to %s".formatted(valueType, targetType));
            throw new NotchRuntimeException(currentStackTrace(), diag);
        }
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

    public List<Object> evaluate(List<NotchExpression> args) {
        var out = new BetterList<>();
        for (NotchExpression arg : args) {
            Object value = evaluate(arg);
            out.add(value);
        }
        return out;
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

    public boolean inStorage(Key<?> key) {
        return storage.containsKey(key);
    }

    public <T> T storage(Key<T> key) {
        if (!storage.containsKey(key)) {
            throw new IllegalStateException("key " + key + " not found in storage for " + source.id);
        }
        return (T) storage.get(key);
    }

    public <T> T storage(Key<T> key, Supplier<T> supplier) {
        if (!storage.containsKey(key)) {
            return supplier.get();
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
            stackTrace.add(this.element);
        }

        @Override
        public void close() {
            var removed = stackTrace.removeLast();
            assert element == removed;
        }
    }
}
