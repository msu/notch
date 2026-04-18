package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.chisel.Token;

public class ElseIfCommand extends NotchTemplateCommand {
    NotchExpression condition;

    public ElseIfCommand() {
        super("elseif");
    }

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        this.condition = commandParser.parseExpression();
        commandParser.requireEnd("extra tokens after if condition");
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {}

    public NotchExpression getCondition() {
        return condition;
    }
}
