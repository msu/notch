package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.chisel.Token;

public class ElseCommand extends NotchTemplateCommand {
    public ElseCommand() {
        super("else");
    }

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {}

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {}
}
