package bigsky.notch.templates.ast.content;

import bigsky.notch.util.Text;
import bigsky.notch.chisel.Token;

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
