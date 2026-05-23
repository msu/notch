package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.chisel.Token;

public class SetCommand extends NotchTemplateCommand {
    public SetCommand() {
        super("set");
    }

    private Token name;
    private NotchExpression value;

    @Override
    public void parseCommand(NotchParser parser) {
        name = parser.requireIdent("expected variable name");
        parser.require("=", "expected '=' after variable name");
        value = parser.requireExpression("expected expression for #set value");
        parser.requireEnd("extra tokens after #set expression");
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        runtime.defineOrUpdate(name.str(), runtime.evaluate(value));
    }
}
