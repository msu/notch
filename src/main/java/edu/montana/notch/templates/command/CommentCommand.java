package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

public class CommentCommand extends NotchTemplateCommand {
    public CommentCommand() {
        super("comment");
    }

    private NotchTemplateContentBlock body;

    @Override
    public void parseCommand(NotchParser parser) {
    }

    @Override
    public void parseBody(NotchTemplateParser parser) {
        body = parser.parseSterileContentBlock(new EndCommand());
        addChildContent(body);
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
    }
}
