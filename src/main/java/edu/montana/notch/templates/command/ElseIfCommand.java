package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

public class ElseIfCommand extends NotchTemplateCommand {
    private NotchExpression condition;
    private NotchTemplateContentBlock body;

    public ElseIfCommand() {
        super("elseif");
    }

    @Override
    public void parseCommand(NotchParser parser) {
        condition = parser.requireExpression("expected 'if' condition here");
    }

    @Override
    public void parseBody(NotchTemplateParser parser) {
        body = parser.parseContentBlock(new EndCommand(), new ElseIfCommand(), new ElseCommand());
        addChildContent(body);
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
        var conditionValue = runtime.evaluate(condition);
        if (runtime.isTruthy(conditionValue)) {
            body.render(runtime, out);
        } else {
            body.terminalCommand().render(runtime, out);
        }

    }
}
