package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.templates.ast.QualifiedIdent;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Token;

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
