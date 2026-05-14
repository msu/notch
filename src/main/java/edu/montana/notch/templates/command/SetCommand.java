package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.chisel.Token;

import static edu.montana.notch.chisel.type.TokenTypePunct.EQ;

public class SetCommand extends NotchTemplateCommand {
    public SetCommand() {
        super("set");
    }

    private Token name;
    private NotchExpression value;

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        name = commandParser.requireIdent("expected variable name");
        commandParser.require(EQ, "expected '=' after variable name");
        value = commandParser.requireExpression("expected expression for #set value");
        commandParser.requireEnd("extra tokens after #set expression");
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        runtime.defineOrUpdate(name.str(), runtime.evaluate(value));
    }
}
