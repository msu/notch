package bigsky.notch.runtime;

import bigsky.utils.chisel.Span;

public record NotchStackTraceElement(String file, Span span) {
}