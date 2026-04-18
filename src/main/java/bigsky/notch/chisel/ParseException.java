package bigsky.notch.chisel;

import bigsky.notch.util.Text;

import java.util.Objects;

public class ParseException extends RuntimeException implements Spanned {
    public final String fileId;
    public final Span span;

    public ParseException(String message, Throwable cause, String fileId, Span span) {
        super(message, cause);
        this.fileId = Objects.requireNonNull(fileId);
        this.span = span;
    }

    public ParseException(String message, String fileId, Location location) {
        this(message, fileId, new Span(location));
    }

    public ParseException(String message, String fileId, Span span) {
        this(message, null, fileId, span);
    }

    public ParseException(String message, Throwable cause, String fileId, Location location) {
        this(message, cause, fileId, new Span(location));
    }

    public ParseException(Throwable cause, String fileId, Span span) {
        this(null, cause, fileId, span);
    }

    @Override
    public String getMessage() {
        var out = new StringBuilder("\n");
        out.append("A parse exception occurred...\n");
        var line = span.start().line;
        var col = span.start().column;
        out.append("  in %s at %d:%d\n".formatted(Text.repr(fileId), line, col));
        out.append("  : ").append(super.getMessage()).append('\n');
        return out.toString();
    }

    @Override
    public Span span() {
        return span;
    }
}
