package edu.montana.notch.templates.ast.content;

import edu.montana.notch.util.Text;
import edu.montana.notch.chisel.Token;

public final class NotchTemplateContentText extends NotchTemplateContentItem {
    public final String content;

    public NotchTemplateContentText(Token content) {
        super(content.start, content.end);
        this.content = content.str();
    }

    @Override
    public String toString() {
        return "Text(%s)".formatted(Text.repr(content));
    }
}
