package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Span;

public record NotchStackTraceElement(Span span, String hint) {
}