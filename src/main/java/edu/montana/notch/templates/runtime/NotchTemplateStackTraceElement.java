package edu.montana.notch.templates.runtime;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchStackTraceElement;
import edu.montana.notch.templates.NotchTemplateCommand;

import java.util.Objects;

/// a frame of template rendering, reported like `#include(main.html:3)` or `${...}(card.html:7)`
public record NotchTemplateStackTraceElement(String name, Span callsite) implements NotchStackTraceElement {
    public NotchTemplateStackTraceElement {
        Objects.requireNonNull(name);
        Objects.requireNonNull(callsite);
    }

    public static NotchTemplateStackTraceElement of(NotchTemplateCommand command) {
        return new NotchTemplateStackTraceElement("#" + command.commandName, command.span());
    }

    public static NotchTemplateStackTraceElement of(NotchExpression expression) {
        return new NotchTemplateStackTraceElement("${...}", expression.span);
    }

    public String templateId() {
        return callsite.source().id;
    }

    @Override
    public String report() {
        return "%s(%s:%d)".formatted(name, templateId(), callsite.startLine());
    }
}
