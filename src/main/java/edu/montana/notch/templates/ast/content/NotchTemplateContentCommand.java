package edu.montana.notch.templates.ast.content;

import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.util.Text;

public final class NotchTemplateContentCommand extends NotchTemplateContentItem {
    public final NotchTemplateCommand command;

    public NotchTemplateContentCommand(NotchTemplateCommand command) {
        super(command.span());
        this.command = command;
    }

    @Override
    public String toString() {
        return "Command(%s)".formatted(Text.repr(command.commandName));
    }
}
