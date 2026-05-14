package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.chisel.Token;

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
