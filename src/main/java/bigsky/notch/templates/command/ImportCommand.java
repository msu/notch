package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.runtime.NotchDiagnostic;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.notch.types.TypeSystem;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.ast.QualifiedIdent;
import bigsky.notch.templates.ast.NotchTemplateContentBlock;
import bigsky.notch.templates.runtime.RenderException;
import bigsky.notch.templates.runtime.NotchTemplateImportable;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.util.BetterMap;
import bigsky.notch.util.Text;
import bigsky.notch.chisel.ParseException;
import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.type.TokenTypeIdentifier;
import bigsky.notch.chisel.type.TokenTypeString;

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
