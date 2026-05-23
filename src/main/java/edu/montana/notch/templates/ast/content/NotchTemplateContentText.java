package edu.montana.notch.templates.ast.content;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.util.Text;

public final class NotchTemplateContentText extends NotchTemplateContentItem {
    public final String content;

    public NotchTemplateContentText(Token content) {
        super(content.span);
        this.content = content.str();
    }

    public NotchTemplateContentText(Span span) {
        super(span);
        this.content = span.rawContent();
    }

    @Override
    public String toString() {
        return "Text(%s)".formatted(Text.repr(content));
    }
}
