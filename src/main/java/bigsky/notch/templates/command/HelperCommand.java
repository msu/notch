package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.types.NotchType;
import bigsky.notch.types.TypeSystem;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.templates.ast.QualifiedIdent;
import bigsky.notch.chisel.ParseException;
import bigsky.notch.chisel.Token;

import java.lang.reflect.InvocationTargetException;

public class HelperCommand extends NotchTemplateCommand implements NotchTemplateCommand.Global, NotchTemplateCommand.Singleton {
    public HelperCommand() {
        super("helper");
    }

    private QualifiedIdent path;

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        path = QualifiedIdent.parse(commandParser);
        if (path == null) {
            throw new ParseException("expected helper class path", fileId, commandParser.location());
        }
        end = path.end;
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {}

    @Override
    public void preRender(NotchTemplateRuntime runtime) {
        NotchType type = TypeSystem.getType(path.qualifiedClass().getName());
        var helper = type.newInstance(new Object[0]);
        runtime.setHelper(helper);
    }
}
