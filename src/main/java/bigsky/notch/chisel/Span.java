package bigsky.notch.chisel;

import java.util.Objects;

public record Span(Location start, Location end) {
    public static final Span CALLSITE = new Span(Location.SOF, Location.EOF);

    public Span(Location start, Location end) {
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        assert (start.index <= end.index) || (start == Location.EOF && end == Location.EOF) || (start == Location.SOF && end == Location.SOF);
    }

    public Span(Location loc) {
        this(loc, loc);
    }

    public static Span mkSpan(Location start, Location end) {
        return new Span(start, end);
    }

    public static Span mkSpan(Location loc) {
        return new Span(loc, loc);
    }
}
