package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;

import java.util.Objects;

public class NotchRuntimeException extends RuntimeException {
    public final NotchStackTrace stackTrace;
    public final Diagnostic diagnostic;

    public NotchRuntimeException(NotchStackTrace stackTrace, Diagnostic diagnostic) {
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.diagnostic = Objects.requireNonNull(diagnostic);
    }

    private static Throwable filterControlSignals(Throwable t) {
        if (t instanceof NotchControlFlow) {
            throw new IllegalStateException("control flow was improperly caught!");
        }
        return t;
    }

    public NotchRuntimeException(NotchStackTrace stackTrace, Diagnostic diagnostic, Throwable t) {
        super(t);
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.diagnostic = Objects.requireNonNull(diagnostic);
    }

    public static NotchRuntimeException userThrow(NotchStackTrace stackTrace, Span span, Throwable value) {
        Diagnostic diag = new Diagnostic();
        diag.setTitle(titleFor(value));
        diag.highlight(span);
        return new NotchRuntimeException(stackTrace, diag, value);
    }

    private static String titleFor(Throwable t) {
        String msg = t.getMessage();
        return msg != null ? t.getClass().getSimpleName() + ": " + msg : t.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        var sb = new StringBuilder();
        sb.append('\n');
        diagnostic.render(sb, false);
        stackTrace.render(sb, false);
        return sb.toString();
    }
}
