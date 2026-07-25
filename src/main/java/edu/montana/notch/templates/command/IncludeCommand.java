package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

public class IncludeCommand extends NotchTemplateCommand {
    public IncludeCommand() {
        super("include");
    }

    private NotchExpression path;

    @Override
    public void parseCommand(NotchParser parser) {
        path = parser.requireStringExpression("expected a file path here");
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
        var pathValue = runtime.evaluate(path);
        var pathStr = runtime.cast(String.class, pathValue, path);
        runtime.templates().renderTemplate(pathStr, runtime, out);
    }
}
