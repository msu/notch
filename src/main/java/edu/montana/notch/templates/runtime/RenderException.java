package edu.montana.notch.templates.runtime;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.ParseErrors;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.runtime.NotchStackTrace;
import edu.montana.notch.util.BetterList;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

/// thrown when a template fails to parse or render.
///
/// it collects the underlying failures as children (parse errors, notch runtime errors, plain java exceptions);
/// the first one is also the cause, the rest are suppressed. `render()` produces the user-facing report:
/// each diagnostic followed by its template stack trace.
public class RenderException extends RuntimeException {
    private final BetterList<Exception> children;

    public RenderException(String message) {
        super(message);
        children = new BetterList<>();
    }

    public RenderException(Exception e) {
        this(List.of(e));
    }

    public RenderException(List<? extends Exception> errors) {
        children = new BetterList<>();
        for (var e : errors) {
            // nested render exceptions are flattened, there's only ever one level of children
            if (e instanceof RenderException re) {
                children.addAll(re.children);
            } else {
                children.add(e);
            }
        }
        if (!children.isEmpty()) initCause(children.get(0));
        for (int i = 1; i < children.size(); i++) {
            addSuppressed(children.get(i));
        }
    }

    /// `e` itself if it is already a render exception, otherwise `e` wrapped in one
    public static RenderException wrap(Exception e) {
        if (e instanceof RenderException re) return re;
        return new RenderException(e);
    }

    public List<Exception> getChildren() {
        return children;
    }

    public <T extends Exception> List<T> getChildren(Class<T> clazz) {
        return children.filter(clazz);
    }

    public <T extends Exception> Optional<T> first(Class<T> clazz) {
        return getChildren(clazz).stream().findFirst();
    }

    /// every diagnostic carried by the children, in order
    public List<Diagnostic> getDiagnostics() {
        var out = new BetterList<Diagnostic>();
        for (var ex : children) {
            var diagnostics = diagnosticsOf(ex);
            if (diagnostics != null) out.addAll(diagnostics);
        }
        return out;
    }

    /// the template stack trace of the first runtime error, if there is one
    public Optional<NotchStackTrace> getNotchStackTrace() {
        return first(NotchRuntimeException.class).map(e -> e.stackTrace);
    }

    /// where in the templates things first went wrong, if any diagnostic says
    public Optional<Span> getSpan() {
        for (var diagnostic : getDiagnostics()) {
            var spans = diagnostic.getSpans();
            if (!spans.isEmpty()) return Optional.of(spans.get(0));
        }
        return Optional.empty();
    }

    public String render() {
        return render(false);
    }

    /// `verbose` adds the internal java traces: where each diagnostic was created and every stack frame
    public String render(boolean verbose) {
        var msg = new StringBuilder();
        for (final var ex : children) {
            if (!msg.isEmpty()) msg.append('\n');
            var diagnostics = diagnosticsOf(ex);
            if (diagnostics != null) {
                for (var diagnostic : diagnostics) {
                    diagnostic.render(msg, verbose);
                }
                if (ex instanceof NotchRuntimeException e) {
                    var trace = verbose ? e.stackTrace : e.stackTrace.trimCallers();
                    trace.render(msg, verbose);
                }
            } else {
                msg.append("uncaught exception:\n");
                var sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                msg.append(sw).append('\n');
            }
        }

        String superMessage = super.getMessage();
        if (superMessage != null) {
            msg.append("Internal Error: ");
            msg.append(superMessage);
        }

        return msg.toString();
    }

    private static List<Diagnostic> diagnosticsOf(Exception ex) {
        if (ex instanceof ParseException e) return List.of(e.diagnostic);
        if (ex instanceof TokenizeException e) return List.of(e.diagnostic);
        if (ex instanceof NotchRuntimeException e) return List.of(e.diagnostic);
        if (ex instanceof ParseErrors e) return e.diagnostics;
        return null;
    }

    @Override
    public String getMessage() {
        return render();
    }
}
