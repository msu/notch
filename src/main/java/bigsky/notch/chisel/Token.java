package bigsky.notch.chisel;

import bigsky.notch.util.Text;
import bigsky.notch.chisel.type.TokenTypeBoundary;

import java.util.Objects;

public class Token implements Spanned {
    public static final Token EOF = new Token(Location.EOF, Location.EOF, TokenTypeBoundary.EOF);
    public static final Token SOF = new Token(Location.SOF, Location.SOF, TokenTypeBoundary.SOF);

    public final Location start, end;
    public final Object data;
    public final TokenType type;

    public Token(Location start, Location end, TokenType type) {
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        this.data = null;
        this.type = Objects.requireNonNull(type);
    }

    public Token(Location start, Location end, TokenType type, Object data) {
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        this.data = data;
        this.type = Objects.requireNonNull(type);
    }

    public Token withType(TokenType other) {
        return new Token(start, end, other, data);
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
        return "Token(at %s@%d %s@%d, %s, %s)".formatted(start.display(), start.index, end.display(), end.index, type, Text.repr("" + data));
    }

    @Override
    public Span span() {
        return new Span(start, end);
    }
}
