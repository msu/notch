package edu.montana.notch.chisel;

import edu.montana.notch.util.Text;

import java.util.Objects;

public class Token implements Spanned {
    public final Span span;
    public final Object data;
    public final String type;

    public Token(Span span, String type) {
        this.span = Objects.requireNonNull(span);
        this.data = null;
        this.type = Objects.requireNonNull(type);
    }

    public Token(Span span, String type, Object data) {
        this.span = Objects.requireNonNull(span);
        this.data = data;
        this.type = Objects.requireNonNull(type);
    }

    public Token withType(String tokenType) {
        return new Token(span, tokenType, data);
    }

    public String str() {
        return String.valueOf(data);
    }

    public boolean bool() {
        return (boolean) (Boolean) data;
    }

    public int integer() {
        return (int) (Integer) data;
    }

    @Override
    public String toString() {
        return "Token(type=%s at %s@%d)".formatted(
                Text.repr(type), span.source().id, span.start().index);
    }

    @Override
    public Span span() {
        return span;
    }
}
