package edu.montana.notch.runtime;

import edu.montana.notch.types.NotchBuiltinMethod;
import edu.montana.notch.types.NotchMethod;
import edu.montana.notch.types.NotchProperty;
import edu.montana.notch.types.TypeRegistry;
import edu.montana.notch.util.Key;
import edu.montana.notch.util.Pair;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

import static edu.montana.notch.util.Pair.pair;

public class NotchBuiltins {
    public static final Key<Random> kRANDOM = new Key<>("random");

    public void print(NotchRuntime runtime, Object object) {
        runtime.println(object);
    }

    public void write(NotchRuntime runtime, Object object) {
        runtime.print(object);
    }

    public String ask(NotchRuntime runtime, String prompt) {
        System.out.print(prompt);
        System.out.flush();
        // TODO: this should use runtime-input
        return new Scanner(System.in).nextLine();
    }

    private Random getRandom(NotchRuntime runtime) {
        return runtime.storage(kRANDOM, (Supplier<Random>) Random::new);
    }

    public float random(NotchRuntime runtime) {
        return getRandom(runtime).nextFloat();
    }

    public Number random(NotchRuntime runtime, Number max) {
        return switch (max) {
            case Integer i -> getRandom(runtime).nextInt(i);
            case Float f -> getRandom(runtime).nextFloat(f);
            case Double d -> getRandom(runtime).nextDouble(d);
            case null, default -> getRandom(runtime).nextLong();
        };
    }

    public Number random(NotchRuntime runtime, Number min, Number max) {
        if (min instanceof Float || max instanceof Float) {
            return getRandom(runtime).nextFloat(min.floatValue(), max.floatValue());
        } else if (min instanceof Double || max instanceof Double) {
            return getRandom(runtime).nextDouble(min.doubleValue(), max.doubleValue());
        } else if (min instanceof Integer || max instanceof Integer) {
            return getRandom(runtime).nextInt(min.intValue(), max.intValue());
        } else {
            return getRandom(runtime).nextLong(min.longValue(), max.longValue());
        }
    }

    public Object sample(NotchRuntime runtime, Collection<Object> values) {
        Random r = getRandom(runtime);
        int i = r.nextInt(values.size());
        return values.stream().skip(i).findFirst().get();
    }

    public String structure(NotchRuntime runtime, Object value) {
        if (value == null) return "<null>";
        if (runtime.isUndefined(value)) return "<undefined>";

        var type = TypeRegistry.getRuntimeType(value);
        // TODO: for props & methods, include the first doc-comment line
        // TODO: enum, primitive reprs, notch-method source code
        var out = new StringBuilder();
        out.append("class ").append(type.getSimpleName()).append(" (").append(type.getNamespace()).append(")\n");
        out.append("  properties:\n");
        for (var prop : type.getProperties()) {
            propertyStructure(out, prop);
        }
        out.append("  methods:\n");
        for (var method : type.getMethods()) {
            methodStructure(out, method);
        }
        return out.toString();
    }

    private void propertyStructure(StringBuilder out, NotchProperty prop) {
        var ty = prop.getType();
        out.append("  - ").append(prop.getName());
        if (prop.isStatic()) {
            out.append(" (static)");
        }
        out.append(": ").append(ty.getDisplayName()).append("\n");
    }

    private void methodStructure(StringBuilder out, NotchMethod method) {
        var variants = method.getVariants();
        String lineStart;
        if (method.isStatic()) {
            lineStart = "  - %s: ".formatted(method.getName());
        } else {
            lineStart = "  - static %s: ".formatted(method.getName());
        }
        out.append(lineStart);
        if (variants == null || variants.isEmpty()) {
            out.append("Object(...)\n");
        } else {
            String padding = " ".repeat(lineStart.length());
            for (int i = 0; i < variants.size(); i++) {
                if (i > 0) out.append(padding);
                var desc = variants.get(i);
                out.append(desc.returnType().getSimpleName()).append("(");
                var params = desc.parameters();
                for (int j = 0; j < params.size(); j++) {
                    var param = params.get(j);
                    if (j > 0) out.append(", ");
                    out.append(param.second()).append(": ").append(param.first());
                }
                out.append(")\n");
            }
        }
    }

    private static Pair<Object, Map<String, Object>> BUILTINS;
    public static Map<String, Object> builtins() {
        if (BUILTINS == null) {
            var root = new NotchBuiltins();
            var builtins = getBuiltins(root);
            BUILTINS = pair(root, builtins);
        }
        return BUILTINS.second();
    }

    public static Map<String, Object> getBuiltins(Object root) {
        final var out = new HashMap<String, Object>();
        final var clazz = root.getClass();
        for (var method : clazz.getMethods()) {
            if (method.getDeclaringClass() == Object.class) continue;
            if (out.containsKey(method.getName())) continue;
            if (Modifier.isStatic(method.getModifiers())) continue;
            final var notchMethod = new NotchBuiltinMethod(method.getName(), clazz);
            out.put(method.getName(), new NotchBoundMethod(root, notchMethod));
        }
        return out;
    }
}
