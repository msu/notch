package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.expressions.NotchMethodInvocation;
import bigsky.notch.runtime.NotchDiagnostic;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.runtime.NotchTemplateRenderable;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.util.BetterList;
import bigsky.notch.chisel.ParseException;
import bigsky.notch.chisel.Token;

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
