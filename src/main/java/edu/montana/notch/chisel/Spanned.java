package edu.montana.notch.chisel;

public interface Spanned {
    Span span();

    default Location start() {
        return span().start();
    }

    default Location end() {
        return span().end();
    }

    default int startLine() {
        return span().start().line;
    }

    default int endLine() {
        return span().end().line;
    }

    default String sourceId() {
        return span().sourceId();
    }

    default Source source() {
        return span().source();
    }

    default String rawContent() {
        final var span = span();
        final var start = Math.max(0, span.start().index);
        final var end = Math.min(source().content.length(), span.end().index);
        return source().content.subSequence(start, end).toString();
    }
}
