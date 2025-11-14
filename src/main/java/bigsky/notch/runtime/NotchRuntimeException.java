package bigsky.notch.runtime;

import bigsky.utils.chisel.Span;
import bigsky.utils.chisel.Spanned;

import java.util.Objects;

public class NotchRuntimeException extends RuntimeException implements Spanned {
    public final NotchStackTrace stackTrace;
    public final NotchDiagnostic diagnostic;

    public NotchRuntimeException(NotchStackTrace stackTrace, NotchDiagnostic diagnostic) {
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.diagnostic = Objects.requireNonNull(diagnostic);
    }

    public NotchRuntimeException(NotchStackTrace stackTrace, NotchDiagnostic diagnostic, Throwable t) {
        super(t);
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.diagnostic = Objects.requireNonNull(diagnostic);
    }

    @Override
    public Span span() {
        return stackTrace.span();
    }
}
