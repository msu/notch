package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Span;

public record NotchStackTraceElement(String file, Span span, String hint) {
}