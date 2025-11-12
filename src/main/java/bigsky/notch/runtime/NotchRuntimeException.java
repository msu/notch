package bigsky.notch.runtime;

import bigsky.utils.chisel.Span;
import bigsky.utils.chisel.Spanned;

import java.util.Objects;

public class NotchRuntimeException extends RuntimeException implements Spanned {
    public final NotchStackTrace stackTrace;
    public final Span span;

    public NotchRuntimeException(NotchStackTrace stackTrace, Span span, String message) {
        super(message);
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.span = null;
    }

    public NotchRuntimeException(NotchStackTrace stackTrace, String message) {
        super(message);
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.span = null;
    }

    public NotchRuntimeException(NotchStackTrace stackTrace, Throwable t, String message) {
        super(message, t);
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.span = null;
    }

    public NotchRuntimeException(NotchStackTrace stackTrace, Throwable t) {
        super(t);
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.span = null;
    }

    public NotchRuntimeException(NotchStackTrace notchStackTrace, Span span, Exception e) {
        super(e);
        this.stackTrace = Objects.requireNonNull(notchStackTrace);
        this.span = Objects.requireNonNull(span);
    }

    @Override
    public Span span() {
        return stackTrace.span();
    }
}
