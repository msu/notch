package edu.montana.notch.chisel;

import java.util.Objects;

public record Span(Source source, Location start, Location end) implements Spanned {
    public Span(Source source, Location start, Location end) {
        this.source = source;
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        assert (start.index <= end.index) || (start == Location.EOI && end == Location.EOI) || (start == Location.SOI && end == Location.SOI);
    }

    public Span(Source source, Location loc) {
        this(source, loc, loc);
    }

    @Override
    public Span span() {
        return this;
    }

    @Override
    public Location start() {
        return start;
    }

    @Override
    public Location end() {
        return end;
    }

    @Override
    public String sourceId() {
        return source.id;
    }

    public Span through(Spanned spanned) {
        return through(spanned.span());
    }

    public Span through(Span span) {
        assert (this.end.index <= span.end.index);
        assert (this.source.id.equals(span.source.id));
        return new Span(source, start, end);
    }

    public Span through(Location end) {
        assert (this.end.index <= end.index);
        return new Span(source, start, end);
    }
}
