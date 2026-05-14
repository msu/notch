package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.runtime.NotchDiagnostic;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.ast.QualifiedIdent;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.types.NotchType;
import bigsky.notch.types.TypeSystem;
import bigsky.notch.util.BetterList;
import bigsky.notch.util.Text;
import bigsky.notch.chisel.ParseException;
import bigsky.notch.chisel.Token;

import static bigsky.notch.chisel.type.TokenTypePunct.BANG;
import static bigsky.notch.chisel.type.TokenTypePunct.COLON;
import static bigsky.notch.chisel.type.TokenTypePunct.COMMA;

public class RequireCommand extends NotchTemplateCommand implements NotchTemplateCommand.Global {
    public RequireCommand() {
        super("require");
    }

    private final BetterList<Requirement> requirements = new BetterList<>();

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        while (true) {
            Token name = commandParser.requireIdent("expected symbol name");
            commandParser.require(COLON, "expected ':' after symbol name");
            QualifiedIdent type = QualifiedIdent.parse(commandParser);
            if (type == null) {
                throw new ParseException("expected type after ':'", fileId, commandParser.location());
            }
            boolean nonNull = commandParser.take(BANG);
            requirements.add(new Requirement(name, type, nonNull));
            if (!commandParser.take(COMMA)) {
                break;
            }
        }
        commandParser.requireEnd("extra tokens after #require");
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
    }

    @Override
    public void preRender(NotchTemplateRuntime runtime) {
        for (var req : requirements) {
            NotchType declaredType = TypeSystem.getType(req.type.qualifiedName());
            if (declaredType == null) {
                throw diagnose(runtime, req.name,
                        "unknown type " + Text.repr(req.type.qualifiedName()));
            }

            String name = req.name.str();
            Object value = runtime.getSymbol(name);
            if (runtime.isUndefined(value)) {
                throw diagnose(runtime, req.name,
                        "missing required symbol " + Text.repr(name)
                                + " (expected " + req.type.qualifiedName() + ")");
            }

            if (value == null) {
                if (req.nonNull) {
                    throw diagnose(runtime, req.name,
                            "required symbol " + Text.repr(name) + " must not be null");
                }
                continue;
            }

            Class<?> expected = declaredType.getBackingClass();
            if (!isAssignable(expected, value.getClass())) {
                throw diagnose(runtime, req.name,
                        value.getClass().getName() + " is not assignable to " + req.type.qualifiedName());
            }
        }
    }

    private static boolean isAssignable(Class<?> expected, Class<?> actual) {
        if (expected.isAssignableFrom(actual)) return true;
        if (expected.isPrimitive()) {
            return primitiveWrapper(expected) == actual;
        }
        return false;
    }

    private static Class<?> primitiveWrapper(Class<?> prim) {
        if (prim == boolean.class) return Boolean.class;
        if (prim == byte.class) return Byte.class;
        if (prim == char.class) return Character.class;
        if (prim == short.class) return Short.class;
        if (prim == int.class) return Integer.class;
        if (prim == long.class) return Long.class;
        if (prim == float.class) return Float.class;
        if (prim == double.class) return Double.class;
        return null;
    }

    private NotchRuntimeException diagnose(NotchTemplateRuntime runtime, Token at, String message) {
        var d = new NotchDiagnostic();
        d.highlight(fileId, at.span());
        d.note(message);
        return new NotchRuntimeException(runtime.currentStackTrace(), d);
    }

    private record Requirement(Token name, QualifiedIdent type, boolean nonNull) {}
}
