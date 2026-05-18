package edu.montana.notch.chisel;

public interface Spanned {
    Span span();

    default Location start() {
        return span().start();
    }

    default Location end() {
        return span().end();
    }

    default String sourceId() {
        return span().sourceId();
    }

    default Source source() {
        return span().source();
    }
}
