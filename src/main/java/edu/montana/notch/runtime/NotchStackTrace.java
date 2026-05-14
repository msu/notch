package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;

import java.util.Objects;

public class NotchStackTrace implements Spanned {
    public NotchStackTraceElement[] elements;

    public NotchStackTrace(NotchStackTraceElement[] elements) {
        this.elements = Objects.requireNonNull(elements);
    }

    @Override
    public Span span() {
        return elements[elements.length - 1].span();
    }
}