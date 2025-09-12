package bigsky.notch.runtime;

import bigsky.utils.chisel.Location;
import bigsky.utils.chisel.Span;
import bigsky.utils.chisel.Spanned;

public class NotchRuntimeException extends RuntimeException implements Spanned {
    public final Span span;

    public NotchRuntimeException(Span span, String message) {
        super(message);
        this.span = span;
    }

    public NotchRuntimeException(Location start, Location end, String message) {
        super(message);
        this.span = new Span(start, end);
    }

    public NotchRuntimeException(Span span, String message, Throwable t) {
        super(message, t);
        this.span = span;
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        s += " (at %s)".formatted(span.start().display());
        String message = getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

    @Override
    public Span span() {
        return span;
    }
}
