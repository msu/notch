package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.ast.NotchTemplateContentBlock;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.chisel.Token;

public class CommentCommand extends NotchTemplateCommand {
    public CommentCommand() {
        super("comment");
    }

    private NotchTemplateContentBlock body;

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        commandParser.requireEnd("extra tokens after #comment");
        body = tmplParser.parseContentBlock(EndCommand.class);
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
    }
}
