package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.chisel.Token;

public class ElseIfCommand extends NotchTemplateCommand {
    NotchExpression condition;

    public ElseIfCommand() {
        super("elseif");
    }

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        this.condition = commandParser.requireExpression("expected condition after #elseif");
        commandParser.requireEnd("extra tokens after if condition");
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {}

    public NotchExpression getCondition() {
        return condition;
    }
}
