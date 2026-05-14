package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.runtime.NotchDiagnostic;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.types.TypeSystem;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.QualifiedIdent;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.RenderException;
import edu.montana.notch.templates.runtime.NotchTemplateImportable;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.util.BetterMap;
import edu.montana.notch.util.Text;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.type.TokenTypeIdentifier;
import edu.montana.notch.chisel.type.TokenTypeString;

public class ImportCommand extends NotchTemplateCommand implements NotchTemplateCommand.Global {
    public ImportCommand() {
        super("import");
    }

    Token path;
    QualifiedIdent type;
    Token alias;

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        if (commandParser.peek(TokenTypeString.STR)) {
            path = commandParser.take();
        } else if (commandParser.peek(TokenTypeIdentifier.IDENT)) {
            type = QualifiedIdent.parse(commandParser);
        } else {
            throw new ParseException("expected type name or template path", fileId, commandParser.peek().span());
        }

        if (commandParser.takeKeyword("as")) {
            alias = commandParser.requireIdent("expected alias");
        }

        commandParser.requireEnd("unexpected tokens after import");
    }

    NotchTemplateContentBlock contentBlock;
    NotchTemplateContentBlock.GlobalCommands info;
    
    @Override
    public void preRender(NotchTemplateRuntime runtime) {
        if (type != null) {
            var qname = type.qualifiedName();
            var notchType = TypeSystem.getType(qname);
            if (notchType == null) {
                var diag = new NotchDiagnostic();
                diag.highlight(fileId, type.span());
                diag.note("I don't know what this type is: %s".formatted(Text.repr(qname)));
                throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
            }
            runtime.defineOrUpdate(notchType.getSimpleName(), notchType);
        } else if (path != null) {
            var templates = runtime.templates();
            var content = templates.getLoader().loadTemplate(path.str());
            var parser = new NotchTemplateParser(templates, path.str(), content);
            try {
                contentBlock = parser.parseContentBlock();
            } catch (ParseException e) {
                throw new RenderException(e.span(), "in " + Text.repr(path.str()), e);
            }

            info = contentBlock.collectGlobalCommands();
            for (var command : info.globals()) {
                command.preRender(runtime);
            }

            if (alias == null) {
                for (var global : info.globals(NotchTemplateImportable.class)) {
                    var values = global.getExportedValues();
                    for (var value : values.entrySet()) {
                        runtime.defineOrUpdate(value.getKey(), value.getValue());
                    }
                }
            } else {
                var imports = new BetterMap<String, Object>();
                for (var global : info.globals(NotchTemplateImportable.class)) {
                    var values = global.getExportedValues();
                    imports.putAll(values);
                }
                runtime.defineOrUpdate(alias.str(), imports);
            }
        } else {
            throw new IllegalStateException("unreachable");
        }
    }

    @Override
    public void postRender(NotchTemplateRuntime runtime, StringBuilder sb) {
        if (info != null) {
            for (Global global : info.globals()) {
                global.postRender(runtime, sb);
            }
        }
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
    }
}
