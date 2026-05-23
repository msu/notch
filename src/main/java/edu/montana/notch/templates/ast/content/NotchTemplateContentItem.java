package edu.montana.notch.templates.ast.content;

import edu.montana.notch.chisel.Span;

public sealed class NotchTemplateContentItem permits NotchTemplateContentCommand, NotchTemplateContentExpression, NotchTemplateContentText {
    public final Span span;

    public NotchTemplateContentItem(Span span) {
        this.span = span;
    }
}
