package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.ast.QualifiedIdent;
import edu.montana.notch.templates.runtime.NotchTemplateImportable;
import edu.montana.notch.templates.runtime.NotchTemplateRenderable;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.util.BetterList;

import java.util.Map;

import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;

public class MacroCommand extends NotchTemplateCommand implements NotchTemplateImportable {
    public MacroCommand() {
        super("macro");
        isGlobal = true;
    }

    private Token name;
    private NotchTemplateContentBlock content;
    private BetterList<Token> parameterNames;
    private BetterList<QualifiedIdent> parameterTypes;

    public final Renderable macro = new Renderable();

    @Override
    public void parseCommand(NotchParser commandParser) {
        name = commandParser.requireIdent("expected macro name");

        if (commandParser.take("(")) {
            parameterNames = new BetterList<>();
            parameterTypes = new BetterList<>();

            while (!commandParser.atEnd() && !commandParser.peek(")")) {
                var name = commandParser.requireIdent("expected parameter name");

                QualifiedIdent type = null;
                if (commandParser.take(":")) {
                    type = QualifiedIdent.parse(commandParser);
                    if (type == null) {
                        final var diag = new Diagnostic()
                                .highlight(commandParser.currentToken())
                                .note("expected parameter type");
                        throw new ParseException(diag);
                    }
                }

                parameterNames.add(name);
                parameterTypes.add(type);

                if (!commandParser.take(",")) {
                    break;
                }
            }

            commandParser.require(")", "unterminated parameter list, expected ')'");
        }

        commandParser.requireEnd("expected end of command");
    }

    @Override
    public void parseBody(NotchTemplateParser parser) {
        // note: we don't add this as child content!
        content = parser.parseContentBlock(new EndCommand());
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
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
            return sourceId() + "#" + name.str();
        }

        @Override
        public void render(BetterList<Object> args, NotchTemplateRuntime runtime, Drain out) {
            var child = new NotchTemplateRuntime(source(), runtime);
            try (var scope = child.pushScope()) {
                scope.define("arguments", args);
                if (parameterNames != null) {
                    for (int i = 0; i < parameterTypes.size(); i++) {
                        var name = parameterNames.get(i);
                        var type = parameterTypes.get(i);

                        var value = args.get(i, UNDEFINED);
                        if (type != null) {
                            var clazz = type.qualifiedClass();

                            if (!runtime.isUndefined(value) && value != null && clazz.isAssignableFrom(value.getClass())) {
                                var diag = new Diagnostic();
                                diag.highlight(name.span());
                                diag.note("undefined is not assignable to " + type.qualifiedName());
                                throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
                            }
                        }
                        scope.define(name.str(), value);
                    }
                }
                content.render(child, out);
            }
        }
    }
}
