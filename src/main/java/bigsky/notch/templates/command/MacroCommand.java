package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.runtime.NotchDiagnostic;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.ast.QualifiedIdent;
import bigsky.notch.templates.ast.NotchTemplateContentBlock;
import bigsky.notch.templates.runtime.NotchTemplateImportable;
import bigsky.notch.templates.runtime.NotchTemplateRenderable;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.util.BetterList;
import bigsky.notch.chisel.ParseException;
import bigsky.notch.chisel.Token;

import java.util.List;
import java.util.Map;

import static bigsky.notch.runtime.NotchRuntime.UNDEFINED;
import static bigsky.notch.chisel.type.TokenTypePunct.*;

public class MacroCommand extends NotchTemplateCommand implements NotchTemplateCommand.Global, NotchTemplateImportable {
    public MacroCommand() {
        super("macro");
    }

    private Token name;
    private NotchTemplateContentBlock content;
    private BetterList<Token> parameterNames;
    private BetterList<QualifiedIdent> parameterTypes;

    public final Renderable macro = new Renderable();

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        name = commandParser.requireIdent("expected fragment name");

        if (commandParser.take(LPAREN)) {
            parameterNames = new BetterList<>();
            parameterTypes = new BetterList<>();

            while (!commandParser.atEnd() && !commandParser.peek(RPAREN)) {
                var name = commandParser.requireIdent("expected parameter name");

                QualifiedIdent type = null;
                if (commandParser.take(COLON)) {
                    type = QualifiedIdent.parse(commandParser);
                    if (type == null) {
                        throw new ParseException("expected parameter type", fileId, commandParser.location());
                    }
                }

                parameterNames.add(name);
                parameterTypes.add(type);

                if (!commandParser.take(COMMA)) {
                    break;
                }
            }

            commandParser.require(RPAREN, "unterminated parameter list, expected ')'");
        }

        commandParser.requireEnd("expected end of command");

        // note: we don't add this as child content!
        content = tmplParser.parseContentBlock(EndCommand.class);
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        runtime.defineOrUpdate(getMacroName(), macro);
    }

    public String getMacroName() {
        return name.str();
    }

    @Override
    public Map<String, Object> getExportedValues() {
        return Map.of(getMacroName(), macro);
    }

    public class Renderable implements NotchTemplateRenderable {
        @Override
        public String getName() {
            return name.str();
        }

        @Override
        public String getQualifiedName() {
            return fileId + ":" + name.str();
        }

        @Override
        public void render(List<Object> args, NotchTemplateRuntime runtime, StringBuilder sb) {
            var child = new NotchTemplateRuntime(fileId, runtime);
            try (var scope = child.pushScope(fileId, span())) {
                scope.define("arguments", args);
                if (parameterNames != null) {
                    for (int i = 0; i < parameterTypes.size(); i++) {
                        var name = parameterNames.get(i);
                        var type = parameterTypes.get(i);

                        var value = i < args.size() ? args.get(i) : UNDEFINED;
                        if (type != null) {
                            var clazz = type.qualifiedClass();

                            if (!runtime.isUndefined(value) && value != null && !clazz.isAssignableFrom(value.getClass())) {
                                var diag = new NotchDiagnostic();
                                diag.highlight(fileId, name.span());
                                diag.note(value.getClass().getName() + " is not assignable to " + type.qualifiedName());
                                throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
                            }
                        }
                        scope.define(name.str(), value);
                    }
                }
                content.render(child, sb);
            }
        }
    }
}
