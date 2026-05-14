package edu.montana.notch.templates.ast.content;

import edu.montana.notch.chisel.Location;

public sealed class NotchTemplateContentItem permits NotchTemplateContentCommand, NotchTemplateContentExpression, NotchTemplateContentText {
    public final Location start, end;

    public NotchTemplateContentItem(Location start, Location end) {
        this.start = start;
        this.end = end;
    }
}
