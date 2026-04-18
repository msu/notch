package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.chisel.Token;

import static bigsky.notch.chisel.type.TokenTypeString.STR;

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
