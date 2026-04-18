package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.chisel.Token;

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
