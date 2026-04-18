package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.templates.ast.NotchTemplateContentBlock;
import bigsky.notch.chisel.Token;

public class ForCommand extends NotchTemplateCommand {

    private Token varName;
    private NotchExpression iterable;
    private NotchTemplateContentBlock content;
    private NotchTemplateContentBlock backupContent;

    public ForCommand() {
        super("for");
    }

    @Override
    public NotchTemplateCommand newInstance() {
        return new ForCommand();
    }

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        varName = commandParser.requireIdent("expected the loop-item variable name");
        commandParser.requireKeyword("in", "expected 'in' after the loop-item variable name");
        iterable = commandParser.requireExpression("expected iterable in 'for' command");
        commandParser.requireEnd("expected end of line after 'for' iterable");

        content = tmplParser.parseContentBlock(EndCommand.class, ElseCommand.class);
        addChildContent(content);

        var endCommand = content.lastCommand();
        if (endCommand instanceof ElseCommand) {
            backupContent = tmplParser.parseContentBlock(EndCommand.class);
            addChildContent(backupContent);
        }
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        var iterableValue = iterable.evaluate(runtime);
        var iterable = runtime.coerceIterable(this.iterable.fileId, this.iterable.span(), iterableValue);

        try (var scope = runtime.pushScope(fileId, span())) {
            int i = 0;
            if (iterable != null) {
                for (Object o : iterable) {
                    scope.define("index", i);
                    scope.define(varName.str(), o);
                    content.render(runtime, sb);
                    i += 1;
                }
            }

            if (i == 0 && backupContent != null) {
                backupContent.render(runtime, sb);
            }
        }
    }
}
