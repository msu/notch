package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

public class ForCommand extends NotchTemplateCommand {

    private Token varName;
    private NotchExpression iterable;
    private NotchTemplateContentBlock content;

    public ForCommand() {
        super("for");
    }

    @Override
    public void parseCommand(NotchParser parser) {
        varName = parser.requireIdent("expected the loop-item variable name");
        parser.requireKeyword("in", "expected 'in' after the loop-item variable name");
        iterable = parser.requireExpression("expected iterable in 'for' command");
        parser.requireEnd("expected end of line after 'for' iterable");
    }

    @Override
    public void parseBody(NotchTemplateParser parser) {
        content = parser.parseContentBlock(new EndCommand(), new ElseCommand());
        addChildContent(content);
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
        var iterableValue = iterable.evaluate(runtime);
        var iterable = runtime.coerceIterable(this.iterable.span(), iterableValue);

        try (var scope = runtime.pushScope()) {
            int i = 0;
            if (iterable != null) {
                for (Object o : iterable) {
                    scope.define("index", i);
                    scope.define(varName.str(), o);
                    content.render(runtime, out);
                    i += 1;
                }
            }

            if (i == 0) {
                content.terminalCommand().render(runtime, out);
            }
        }
    }
}
