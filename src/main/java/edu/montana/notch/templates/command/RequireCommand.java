package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.QualifiedIdent;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.Text;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Token;

public class RequireCommand extends NotchTemplateCommand {
    public RequireCommand() {
        super("require");
        isGlobal = true;
    }

    private final BetterList<Requirement> requirements = new BetterList<>();

    @Override
    public void parseCommand(NotchParser parser) {
        while (true) {
            Token name = parser.requireIdent("expected symbol name");
            parser.require(":", "expected ':' after symbol name");
            var type = parser.requireQualifiedIdent("expected type after ':'");
            boolean nonNull = parser.take("!");
            requirements.add(new Requirement(name, type, nonNull));
            if (!parser.take(",")) {
                break;
            }
        }
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
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
        var d = new Diagnostic();
        d.highlight(at);
        d.note(message);
        return new NotchRuntimeException(runtime.currentStackTrace(), d);
    }

    private record Requirement(Token name, QualifiedIdent type, boolean nonNull) {}
}
