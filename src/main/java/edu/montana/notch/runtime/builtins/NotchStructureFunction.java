package edu.montana.notch.runtime.builtins;

import edu.montana.notch.runtime.NotchBoundMethod;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchMethod;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;
import edu.montana.notch.util.Pair;

import java.util.List;

import static edu.montana.notch.util.Pair.pair;

public final class NotchStructureFunction implements NotchMethod {
    private static NotchBoundMethod INST = null;
    public static NotchBoundMethod getInstance() {
        if (INST == null) {
            INST = new NotchBoundMethod(NotchRuntime.BUILTIN, null, new NotchStructureFunction());
        }
        return INST;
    }
    private NotchStructureFunction() {}

    @Override
    public boolean canInvokeWith(NotchRuntime runtime, List<Object> args) {
        return args.size() == 1;
    }

    @Override
    public String invoke(NotchRuntime runtime, Object rootVal, List<Object> args) {
        var arg = args.get(0);
        if (arg == null) return "null";
        if (runtime.isUndefined(arg)) return "<undefined>";
        var type = TypeSystem.getType(arg.getClass());
        var out = new StringBuilder();
        out.append("class ").append(type.getSimpleName()).append(" (").append(type.getNamespace()).append(")\n");
        out.append("  properties:\n");
        for (var prop : type.getProperties()) {
            var ty = prop.getType();
            out.append("  - ").append(prop.getName()).append(": ").append(ty.getDisplayName()).append("\n");
        }
        out.append("  methods:\n");
        for (var method : type.getMethods()) {
            var variants = method.getMethodVariants();
            var lineStart = "  - %s: ".formatted(method.getName());
            out.append(lineStart);
            if (variants == null || variants.isEmpty()) {
                out.append("Object(...)\n");
            } else {
                String padding = " ".repeat(lineStart.length());
                for (int i = 0; i < variants.size(); i++) {
                    if (i > 0) out.append(padding);
                    var variant = variants.get(i);
                    out.append(variant.second().getSimpleName()).append("(");
                    var params = variant.first();
                    for (int j = 0; j < params.size(); j++) {
                        var param = params.get(j);
                        if (j > 0) out.append(", ");
                        out.append(param.first()).append(": ").append(param.second());
                    }
                    out.append(")\n");
                }
            }
        }
        return out.toString();
    }

    @Override
    public String getName() {
        return "structure";
    }

    @Override
    public String getDisplayName() {
        return "notch.builtins.structure()";
    }

    @Override
    public String getQualifiedName() {
        return "notch.builtins.structure";
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public List<Pair<List<Pair<String, NotchType>>, NotchType>> getMethodVariants() {
        return List.of(pair(
                List.of(pair("object", TypeSystem.getType(Object.class))),
                TypeSystem.getType(String.class)
        ));
    }
}
