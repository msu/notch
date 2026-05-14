package edu.montana.notch.chisel;

import java.util.Objects;

public class TokenizeException extends RuntimeException implements Spanned {
    public final Location start, end;

    public TokenizeException(Location token, String message) {
        super(message);
        this.start = Objects.requireNonNull(token);
        this.end = token;
    }

    public TokenizeException(Location start, Location end, String message) {
        super(message);
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
    }

    public TokenizeException(Span span, String message, Throwable parent) {
        super(message, parent);
        this.start = span.start();
        this.end = span.end();
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        s += " (at %s)".formatted(start.display());
        String message = getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

    @Override
    public Span span() {
        return new Span(start, end);
    }
}
