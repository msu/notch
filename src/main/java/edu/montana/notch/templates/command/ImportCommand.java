package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.ast.QualifiedIdent;
import edu.montana.notch.templates.runtime.NotchTemplateImportable;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.types.TypeSystem;
import edu.montana.notch.util.BetterMap;
import edu.montana.notch.util.Text;

import java.util.List;
import java.util.Map;

public class ImportCommand extends NotchTemplateCommand {
    public ImportCommand() {
        super("import");
        isGlobal = true;
    }

    Token path;
    QualifiedIdent type;
    Token alias;

    @Override
    public void parseCommand(NotchParser commandParser) {
        if (commandParser.peek("string")) {
            path = commandParser.take();
        } else if (commandParser.peek("ident")) {
            type = QualifiedIdent.parse(commandParser);
        } else {
            final var diag = new Diagnostic()
                    .highlight(commandParser.currentToken())
                    .note("expected type name or template path");
            throw new ParseException(diag);
        }

        if (commandParser.takeKeyword("as")) {
            alias = commandParser.requireIdent("expected alias");
        }

        commandParser.requireEnd("unexpected tokens after import");
    }

    NotchTemplateContentBlock contentBlock;
    NotchTemplateRuntime contentRuntime;

    @Override
    public void preRender(NotchTemplateRuntime runtime) {
        if (type != null) {
            var qname = type.qualifiedName();
            var notchType = TypeSystem.getType(qname);
            if (notchType == null) {
                var diag = new Diagnostic();
                diag.highlight(type.span());
                diag.note("I don't know what this type is: %s".formatted(Text.repr(qname)));
                throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
            }
            runtime.defineOrUpdateGlobal(notchType.getSimpleName(), notchType);
        } else if (path != null) {
            contentBlock = templates.getTemplateContent(path.str());

            contentRuntime = new NotchTemplateRuntime(contentBlock.source(), runtime.templates(), Map.of());
            contentBlock.preRender(contentRuntime);

            if (alias == null) {
                for (var global : contentRuntime.globals()) {
                    if (!(global instanceof NotchTemplateImportable imp)) continue;
                    var values = imp.getExportedValues();
                    for (var value : values.entrySet()) {
                        runtime.defineOrUpdateGlobal(value.getKey(), value.getValue());
                    }
                }
            } else {
                var imports = new BetterMap<String, Object>();
                for (var global : contentRuntime.globals()) {
                    if (!(global instanceof NotchTemplateImportable imp)) continue;
                    var values = imp.getExportedValues();
                    imports.putAll(values);
                }
                runtime.defineOrUpdateGlobal(alias.str(), imports);
            }
        } else {
            throw new IllegalStateException("unreachable");
        }
    }

    @Override
    public void postRender(NotchTemplateRuntime runtime) {
        if (contentBlock != null) {
            contentBlock.postRender(contentRuntime);
        }
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
    }
}
