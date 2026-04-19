package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.chisel.Token;

import static bigsky.notch.chisel.type.TokenTypePunct.EQ;

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
