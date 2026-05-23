package edu.montana.notch.chisel;

public class ParseException extends RuntimeException {
    public final Diagnostic diagnostic;

    public ParseException(String message, Throwable cause, Diagnostic diagnostic) {
        super(message, cause);
        this.diagnostic = diagnostic;
    }

    public ParseException(Throwable cause, Diagnostic diagnostic) {
        super(cause);
        this.diagnostic = diagnostic;
    }

    public ParseException(String message, Diagnostic diagnostic) {
        super(message);
        this.diagnostic = diagnostic;
    }

    public ParseException(Diagnostic diag) {
        this.diagnostic = diag;
    }

    @Override
    public String getMessage() {
        return diagnostic.render();
    }
}
