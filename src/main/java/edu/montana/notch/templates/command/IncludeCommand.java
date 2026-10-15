package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.ParseException;
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
        path = parser.parseStringExpression();
        if (path == null) {
            var diag = new Diagnostic();
            diag.highlight(parser.currentToken());
            diag.note("expected included template path");
            throw new ParseException(diag);
        }
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
        var pathValue = runtime.evaluate(path);
        var pathStr = runtime.cast(String.class, pathValue, path);
        runtime.templates().renderTemplate(pathStr, runtime, out);
    }
}
