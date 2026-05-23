package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

public final class EndCommand extends NotchTemplateCommand {
    public EndCommand() {
        super("end");
    }

    @Override
    public void parseCommand(NotchParser parser) {
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
    }
}
