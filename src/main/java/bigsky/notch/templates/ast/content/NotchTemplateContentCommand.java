package bigsky.notch.templates.ast.content;

import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.util.Text;

public final class NotchTemplateContentCommand extends NotchTemplateContentItem {
    public final NotchTemplateCommand command;

    public NotchTemplateContentCommand(NotchTemplateCommand command) {
        super(command.getStart(), command.getEnd());
        this.command = command;
    }

    @Override
    public String toString() {
        return "Command(%s)".formatted(Text.repr(command.name));
    }
}
