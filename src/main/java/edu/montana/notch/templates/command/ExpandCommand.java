package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.expressions.NotchMethodInvocation;
import edu.montana.notch.runtime.NotchDiagnostic;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.runtime.NotchTemplateRenderable;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Token;

public class ExpandCommand extends NotchTemplateCommand {
    public ExpandCommand() {
        super("expand");
    }

    private NotchMethodInvocation macroExpression;

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        var macro = commandParser.requireExpression("expected macro");
        if (!(macro instanceof NotchMethodInvocation nm)) {
            throw new ParseException("expected macro invocation", fileId, macro.span());
        }

        this.macroExpression = nm;
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        var value = runtime.evaluate(macroExpression.root);
        if (!(value instanceof NotchTemplateRenderable macro)) {
            var diag = new NotchDiagnostic();
            diag.highlight(fileId, macroExpression.span());
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
