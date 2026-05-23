package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

public class ElseCommand extends NotchTemplateCommand {
    public ElseCommand() {
        super("else");
    }

    private NotchTemplateContentBlock body;

    @Override
    public void parseCommand(NotchParser commandParser) {
    }

    @Override
    public void parseBody(NotchTemplateParser parser) {
        body = parser.parseContentBlock(new EndCommand());
        addChildContent(body);
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        body.render(runtime, sb);
    }
}
