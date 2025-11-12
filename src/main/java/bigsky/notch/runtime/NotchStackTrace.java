package bigsky.notch.runtime;

import bigsky.utils.chisel.Span;
import bigsky.utils.chisel.Spanned;

public class NotchStackTrace implements Spanned {
    public NotchStackTraceElement[] elements;

    public NotchStackTrace(NotchStackTraceElement[] elements) {
        this.elements = elements;
    }

    @Override
    public Span span() {
        return elements[elements.length - 1].span();
    }
}