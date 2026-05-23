package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;

import java.util.Objects;

public class NotchRuntimeException extends RuntimeException implements Spanned {
    public final NotchStackTrace stackTrace;
    public final Diagnostic diagnostic;

    public NotchRuntimeException(NotchStackTrace stackTrace, Diagnostic diagnostic) {
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.diagnostic = Objects.requireNonNull(diagnostic);
    }

    public NotchRuntimeException(NotchStackTrace stackTrace, Diagnostic diagnostic, Throwable t) {
        super(t);
        this.stackTrace = Objects.requireNonNull(stackTrace);
        this.diagnostic = Objects.requireNonNull(diagnostic);
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
