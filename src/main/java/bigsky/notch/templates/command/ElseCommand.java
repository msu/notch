package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.chisel.Token;

public class ElseCommand extends NotchTemplateCommand {
    public ElseCommand() {
        super("else");
    }

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {}

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {}
}
