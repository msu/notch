package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.chisel.Token;

public final class EndCommand extends NotchTemplateCommand {
    public EndCommand() {
        super("end");
    }

    @Override
    public NotchTemplateCommand newInstance() {
        return new EndCommand();
    }

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        start = commandName.start;
        end = commandName.end;
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {}
}
