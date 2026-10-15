package edu.montana.notch.templates.runtime;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchStackTraceElement;

public record NotchTemplateStackTraceElement(String templateId, Span callsite) implements NotchStackTraceElement {
    @Override
    public String report() {
        return "NotchTemplateStackTraceElement(%s, %s)".formatted(templateId, callsite);
    }
}
