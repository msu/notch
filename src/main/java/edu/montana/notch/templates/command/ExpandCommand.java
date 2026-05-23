package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.expressions.NotchMethodInvocation;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.runtime.NotchTemplateRenderable;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.util.BetterList;

public class ExpandCommand extends NotchTemplateCommand {
    public ExpandCommand() {
        super("expand");
    }

    private NotchMethodInvocation macroExpression;

    @Override
    public void parseCommand(NotchParser commandParser) {
        var macro = commandParser.requireExpression("expected a macro invocation");
        if (!(macro instanceof NotchMethodInvocation nm)) {
            final var diag = new Diagnostic()
                    .highlight(macro.span())
                    .note("expected a macro invocation");
            throw new ParseException(diag);
        }

        this.macroExpression = nm;
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        var value = runtime.evaluate(macroExpression.root);
        if (!(value instanceof NotchTemplateRenderable macro)) {
            var diag = new Diagnostic();
            diag.highlight(macroExpression.span());
            diag.note("unable to invoke type as macro : " + NotchRuntime.className(value));
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }

        var argValues = new BetterList<>();
        for (var arg : macroExpression.args) {
            var argValue = runtime.evaluate(arg);
            argValues.add(argValue);
        }
        macro.render(argValues, runtime, sb);
    }
}
