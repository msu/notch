package bigsky.notch.runtime;

import bigsky.notch.chisel.Span;

public record NotchStackTraceElement(String file, Span span, String hint) {
}