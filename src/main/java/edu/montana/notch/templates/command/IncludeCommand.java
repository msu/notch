package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.chisel.Token;

import static edu.montana.notch.chisel.type.TokenTypeString.STR;

public class IncludeCommand extends NotchTemplateCommand {
    public IncludeCommand() {
        super("include");
    }

    private Token path;

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        path = commandParser.require(STR, "expected template path");
        commandParser.requireEnd("expected EOL after template path");
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        var templatePath = this.path.str();
        var content = runtime.templates().renderTemplate(templatePath, runtime);
        sb.append(content);
    }
}
