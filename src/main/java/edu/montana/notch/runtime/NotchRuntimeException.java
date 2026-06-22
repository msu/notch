package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;

import java.util.Objects;

public class NotchRuntimeException extends RuntimeException implements Spanned {
    public final NotchStackTrace stackTrace;
    public final Diagnostic diagnostic;
    public final Object thrownValue;

    public NotchRuntimeException(NotchStackTrace stackTrace, Diagnostic diagnostic) {
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.diagnostic = Objects.requireNonNull(diagnostic);
        this.thrownValue = null;
    }

    public NotchRuntimeException(NotchStackTrace stackTrace, Diagnostic diagnostic, Throwable t) {
        super(t);
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.diagnostic = Objects.requireNonNull(diagnostic);
        this.thrownValue = null;
    }

    private NotchRuntimeException(NotchStackTrace stackTrace, Diagnostic diagnostic, Throwable cause, Object thrownValue) {
        super(cause);
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.diagnostic = Objects.requireNonNull(diagnostic);
        this.thrownValue = thrownValue;
    }

    public static NotchRuntimeException userThrow(NotchStackTrace stackTrace, Span span, Object value) {
        Diagnostic diag = new Diagnostic();
        diag.setTitle(titleFor(value));
        if (span != null) diag.highlight(span);
        Throwable cause = (value instanceof Throwable t) ? t : null;
        return new NotchRuntimeException(stackTrace, diag, cause, value);
    }

    private static String titleFor(Object value) {
        if (value instanceof Throwable t) {
            String msg = t.getMessage();
            return msg != null ? t.getClass().getSimpleName() + ": " + msg : t.getClass().getSimpleName();
        }
        return String.valueOf(value);
    }

    @Override
    public String getMessage() {
        return diagnostic.render();
    }

    @Override
    public Span span() {
        return stackTrace.span();
    }
}
