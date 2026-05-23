package edu.montana.notch.chisel;

import java.util.List;

public final class Source {
    public final String id;
    public final CharSequence content;

    public final Token soi;
    public final Token eoi;
    public final Span span;

    public Source(
            String id,
            CharSequence content
    ) {
        this.id = id;
        this.content = content;

        this.soi = new Token(new Span(this, Location.SOI, Location.SOI), "soi");
        this.eoi = new Token(new Span(this, Location.EOI, Location.EOI), "eoi");
        this.span = new Span(this, Location.SOI, Location.EOI);
    }

    @Override
    public String toString() {
        return "Source(id=%s)".formatted(id);
    }

    public List<String> lines(int startLine, int endLine) {
        Integer[] i = {startLine};
        return content.toString()
                .lines()
                .skip(startLine - 1)
                .takeWhile(line -> i[0]++ <= endLine)
                .toList();
    }
}
