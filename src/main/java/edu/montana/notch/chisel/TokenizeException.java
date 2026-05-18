package edu.montana.notch.chisel;

public class TokenizeException extends RuntimeException {
    public final Diagnostic diagnostic;

    public TokenizeException(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }

    public TokenizeException(Diagnostic diagnostic, String message) {
        super(message);
        this.diagnostic = diagnostic;
    }

    public TokenizeException(Diagnostic diagnostic, Throwable parent) {
        super(parent);
        this.diagnostic = diagnostic;
    }

    @Override
    public String toString() {
        return diagnostic.render();
    }
}
