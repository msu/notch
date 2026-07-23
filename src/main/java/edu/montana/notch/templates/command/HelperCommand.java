package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.ast.QualifiedIdent;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;

import java.lang.reflect.Constructor;

public class HelperCommand extends NotchTemplateCommand {
    public HelperCommand() {
        super("helper");
        isGlobal = true;
        isSingleton = true;
    }

    private QualifiedIdent path;

    @Override
    public void parseCommand(NotchParser commandParser) {
        path = commandParser.parseQualifiedIdent();
        if (path == null) {
            final var diag = new Diagnostic()
                    .highlight(commandParser.currentToken())
                    .note("expected helper class path");
            throw new ParseException(diag);
        }
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
    }

    @Override
    public void preRender(NotchTemplateRuntime runtime) {
        NotchType type = TypeSystem.getType(path.qualifiedClass().getName());
        Object helper = null;
        try {
            try {
                Constructor cons = type.getBackingClass().getConstructor(NotchTemplateRuntime.class);
                helper = cons.newInstance(runtime);
            } catch (NoSuchMethodException | SecurityException e) {}

            if (helper == null) {
                helper = type.newInstance(new Object[0]);
            }
        } catch (Exception e) {
            final var diag = new Diagnostic();
            diag.highlight(path);
            diag.note("unable to create helper");
            diag.note(e.getMessage());
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
        runtime.setHelper(helper);
    }
}
