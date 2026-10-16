package edu.montana.notch.runtime;

import edu.montana.notch.Notch;
import edu.montana.notch.NotchElement;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.statements.NotchStatement;
import edu.montana.notch.types.*;
import edu.montana.notch.util.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static edu.montana.notch.util.Exceptions.rethrow;

public class NotchRuntime {
    public static final Object UNDEFINED = new HasCustomNotchType() {
        @Override
        public String toString() {
            return "<undefined>";
        }

        @Override
        public NotchType getNotchType() {
            return NotchUndefinedType.INST;
        }
    };
    public static final Key<List<NotchTypeCoercer>> kCoercions = new Key<>("notch.type.coercers");

    public final Source source;
    private LinkedList<Scope> values = new LinkedList<>();
    private final StackTracer stackTracer;
    private Consumer<Object> out = System.out::print;
    private BetterMap<Key, Object> storage = new BetterMap<>();
    private final Deque<Throwable> handledExceptions = new ArrayDeque<>();

    public static final NotchRuntime BUILTIN = new NotchRuntime(new Source("notch.builtins", "._."), Map.of());
    public NotchRuntime(Source source) {
        this(source, Notch.builtins());
    }

    public NotchRuntime(Source source, Map<String, Object> entryBlock) {
        this(source, entryBlock, new StackTracer());
    }

    /// a fresh top-level scope whose stack frames are still recorded in `caller`'s stack trace
    public NotchRuntime(Source source, Map<String, Object> entryBlock, NotchRuntime caller) {
        this(source, entryBlock, caller.stackTracer);
    }

    private NotchRuntime(Source source, Map<String, Object> entryBlock, StackTracer stackTracer) {
        this.source = Objects.requireNonNull(source);
        this.stackTracer = stackTracer;
        Objects.requireNonNull(entryBlock);
        var scope = new Scope(entryBlock);
        values.push(scope);

        storage.put(kCoercions, Notch.coercions());
    }

    public NotchRuntime(Source source, NotchRuntime parent) {
        this.source = source;
        stackTracer = parent.stackTracer;
        out = parent.out;
        values = new LinkedList<>(parent.values);
        storage.put(kCoercions, parent.storage(kCoercions));
    }

    public static String className(Object obj) {
        if (obj == null) {
            return "<null>";
        } else if (obj == UNDEFINED) {
            return "<undefined>";
        }
        return obj.getClass().getName();
    }

    public static Object run(NotchRuntime runtime, NotchElement item) {
       try (var _run = runtime.hideStackFrames()) {
            if (item instanceof NotchExpression e) {
                return runtime.evaluate(e);
            } else if (item instanceof NotchStatement s) {
                runtime.execute(s);
                return null;
            } else {
                final var d = new Diagnostic();
                d.note("don't know how to run " + Text.simpleClassName(item));
                throw new NotchRuntimeException(runtime.currentStackTrace(), d);
            }
        }
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
        return stackTracer.dumpStackTrace();
    }

    public NotchStackTrace getStackTrace(StackTraceElement catchSite, Throwable t) {
        return stackTracer.getStackTrace(catchSite, t);
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
        return NotchTypeSystem.getType(clazz);
    }

    public NotchType typeOf(Object value) {
        return NotchTypeSystem.getRuntimeType(value);
    }

    public Object coerce(NotchType type, Object value, Spanned span) {
        var clazz = type.getBackingClass();
        if (clazz == null) throw new UnsupportedOperationException();
        return coerce(clazz, value, span);
    }

    @SuppressWarnings("unchecked")
    public <T> T coerce(Class<T> clazz, Object value, Spanned span) {
        if (value == null || isUndefined(value) || clazz.isInstance(value)) {
            return cast(clazz, value, span);
        }
        var target = NotchTypeSystem.getType(clazz);
        var source = typeOf(value);
        var coerceResult = getCoercer(source, target);
        if (coerceResult == null) {
            final var d = new Diagnostic()
                    .highlight(span)
                    .note("unable to coerce %s into %s".formatted(source.getDisplayName(), target.getDisplayName()));
            throw new NotchRuntimeException(currentStackTrace(), d);
        }
        try {
            Object result = coerceResult.coercer.coerce(this, value, span.span(), source, target);
            return (T) result;
        } catch (Exception e) {
            final var d = new Diagnostic()
                    .highlight(span)
                    .note("unable to coerce %s into %s".formatted(source.getDisplayName(), target.getDisplayName()));
            if (e instanceof NotchTypeCoercer.CoercionFailure) {
                d.note("this was likely an internal bug with the coercer: %s".formatted(coerceResult.coercer.getClass().getName()));
            }
            throw new NotchRuntimeException(currentStackTrace(), d, e);
        }
    }

    public SafeAutoClosable showStackFrames() {
        var trace = Thread.currentThread().getStackTrace();
        var caller = trace[3];
        return stackTracer.showStackFramesAfter(caller);
    }

    public SafeAutoClosable hideStackFrames() {
        var trace = Thread.currentThread().getStackTrace();
        var caller = trace[3];
        return stackTracer.hideStackFramesAfter(caller);
    }

    public SafeAutoClosable addStackFrame(NotchStackTraceElement element) {
        return stackTracer.addStackFrame(element);
    }

    public record BestCoercer(int cost, NotchTypeCoercer coercer) {}
    public BestCoercer getCoercer(NotchType source, NotchType target) {
        var coercions = storage(kCoercions);
        int minCost = Integer.MAX_VALUE;
        NotchTypeCoercer choice = null;
        for (NotchTypeCoercer coercion : coercions) {
            int cost = coercion.getCoercionCost(source, target);
            if (cost >= 0 && cost < minCost) {
                minCost = cost;
                choice = coercion;
            }
        }
        if (choice == null) return null;
        return new BestCoercer(minCost, choice);
    }

    public <T> T cast(Class<T> clazz, Object value, Spanned item) {
        try {
            return clazz.cast(value);
        } catch (ClassCastException e) {
            String valueType;
            if (value == null) valueType = "null";
            else if (isUndefined(value)) valueType = "undefined";
            else valueType = NotchTypeSystem.getRuntimeType(value).getDisplayName();
            var targetType = NotchTypeSystem.getType(clazz).getDisplayName();

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
        try {
            var value = expression.evaluate(this);
            return value;
        } catch (NotchRuntimeException | NotchControlFlow e) {
            throw rethrow(e);
        } catch (Throwable t) {
            var site = Thread.currentThread().getStackTrace();
            final var st = getStackTrace(site[3], t);
            final var d = new Diagnostic();
            d.highlight(expression.span);
            d.note("threw %s".formatted(t.getClass().getName()));
            var msg = t.getMessage();
            if (msg != null) d.note(msg);
            throw new NotchRuntimeException(st, d, t);
        }
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
        } catch (NotchRuntimeException | NotchControlFlow e) {
            throw rethrow(e);
        } catch (Throwable t) {
            var trace = Thread.currentThread().getStackTrace();
            var st = getStackTrace(trace[2], t);
            var diag = new Diagnostic();
            diag.highlight(stmt.span());
            diag.note("threw %s".formatted(t.getClass().getName()));
            var msg = t.getMessage();
            if (msg != null) diag.note(msg);
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

    public StackTracer getStackTracer() {
        return stackTracer;
    }

    public static class Scope extends LinkedHashMap<String, Object> {
        public Scope() {
        }

        public Scope(Map<? extends String, ?> m) {
            super(m);
        }
    }

    private static class StackTracer {
        private final BetterList<Control> controls = new BetterList<>();

        /// note: `t` must just have been thrown
        NotchStackTrace getStackTrace(StackTraceElement catchsite, Throwable t) {
            var t1 = t.getStackTrace();
            if (t instanceof InvocationTargetException) {
                var t2 = t.getCause().getStackTrace();
                return getStackTrace(catchsite, t2);
            } else {
                return getStackTrace(catchsite, t1);
            }
        }

        public NotchStackTrace dumpStackTrace() {
            var trace = Thread.currentThread().getStackTrace();
            var out = new BetterList<NotchStackTraceElement>();
            applyControls(trace, 2, out);
            return new NotchStackTrace(out.toArray(NotchStackTraceElement[]::new), trace);
        }

        private NotchStackTrace getStackTrace(StackTraceElement catchsite, StackTraceElement[] trace) {
            int localTraceSize = 0;
            for (int i = 0; i < trace.length; i += 1) {
                if (trace[i + 1].equals(catchsite)) break;
                localTraceSize = i;
            }
            if (localTraceSize == trace.length) {
                throw new IllegalStateException("wrong callsite passed!");
            }

            int relevantTraceSize = localTraceSize;
            for (int i = localTraceSize; i >= 0; i--) {
                var elt = trace[i];
                var cname = elt.getClassName();
                if (cname.startsWith("java.lang.reflect") || cname.startsWith("jdk.internal.reflect")) {
                    relevantTraceSize--;
                } else {
                    break;
                }
            }

            var out = new BetterList<NotchStackTraceElement>();
            for (int i = 0; i < relevantTraceSize; i += 1) {
                out.add(new NotchStackTraceElement.Java(trace[i]));
            }


            applyControls(trace, localTraceSize + 2, out);
            return new NotchStackTrace(out.toArray(NotchStackTraceElement[]::new), trace);
        }

        /// walks `trace` from `frameIdx` outwards, newest control first, deciding which java frames to keep
        private void applyControls(StackTraceElement[] trace, int frameIdx, BetterList<NotchStackTraceElement> out) {
            // the start frame of the previous show/hide is never the start of the next one, even when
            // they look equal (e.g. nested includes all start at the same line of IncludeCommand)
            int searchFrom = frameIdx;
            for (int i = controls.size() - 1; i >= 0; i--) {
                var control = controls.get(i);
                if (control instanceof Add it) {
                    out.add(it.element);
                } else if (control instanceof Hide it) {
                    frameIdx = indexOf(trace, searchFrom, it.start);
                    searchFrom = frameIdx + 1;
                } else if (control instanceof Show it) {
                    int start = indexOf(trace, searchFrom, it.start);
                    while (frameIdx < start) {
                        out.add(new NotchStackTraceElement.Java(trace[frameIdx]));
                        frameIdx += 1;
                    }
                    searchFrom = frameIdx + 1;
                }
            }

            while (frameIdx < trace.length) {
                out.add(new NotchStackTraceElement.Java(trace[frameIdx]));
                frameIdx += 1;
            }
        }

        private static int indexOf(StackTraceElement[] trace, int from, StackTraceElement element) {
            for (int i = from; i < trace.length; i++) {
                if (trace[i].equals(element)) return i;
            }
            // the frame was cut off of a very deep trace
            return trace.length;
        }

        public ControlGuard showStackFramesAfter(StackTraceElement caller) {
            return new ControlGuard(new Show(caller));
        }

        public ControlGuard hideStackFramesAfter(StackTraceElement caller) {
            return new ControlGuard(new Hide(caller));
        }

        public ControlGuard addStackFrame(NotchStackTraceElement element) {
            return new ControlGuard(new Add(element));
        }

        sealed interface Control {}
        record Hide(StackTraceElement start) implements Control {}
        record Add(NotchStackTraceElement element) implements Control {}
        record Show(StackTraceElement start) implements Control {}


        class ControlGuard implements SafeAutoClosable {
            final Control control;

            public ControlGuard(Control control) {
                this.control = control;
                controls.add(control);
            }

            @Override
            public void close() {
                var c = controls.removeLast();
                if (c != control) {
                    throw new IllegalStateException("stack controls corrupted");
                }
            }
        }
    }
}
