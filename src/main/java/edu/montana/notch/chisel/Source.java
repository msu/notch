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
        this.eoi = new Token(new Span(this, endOfInput(content)), "eoi");
        this.span = new Span(this, Location.SOI, Location.EOI);
    }

    //position past the last character is somewhere a caret can point.
    private static Location endOfInput(CharSequence content) {
        final var text = content.toString();
        var walked = new Location();
        var lastVisible = walked;
        for (int i = 0; i < text.length(); i++) {
            final var character = text.charAt(i);
            walked = walked.next(character);
            if (character != '\n' && character != '\r') lastVisible = walked;
        }
        return new Location(text.length(), lastVisible.line, lastVisible.column);
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
