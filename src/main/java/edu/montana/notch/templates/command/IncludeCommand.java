package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

public class IncludeCommand extends NotchTemplateCommand {
    public IncludeCommand() {
        super("include");
    }

    private Token path;

    @Override
    public void parseCommand(NotchParser parser) {
        path = parser.require("string", "expected template path");
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
        var templatePath = this.path.str();
        runtime.templates().renderTemplate(templatePath, runtime, out);
    }
}
