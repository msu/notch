package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.statements.NotchField;
import edu.montana.notch.statements.NotchStatement;
import edu.montana.notch.util.Text;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;

public final class NotchClass {
    public final String name;
    private final List<NotchField> headerFields;
    private final List<NotchField> bodyFields;
    private final Map<String, Method> methods;
    private final NotchRuntime captured;
    private final Set<String> declaredFields;

    public NotchClass(String name, List<NotchField> headerFields, List<NotchField> bodyFields,
                      Map<String, Method> methods, NotchRuntime captured) {
        this.name = name;
        this.headerFields = headerFields;
        this.bodyFields = bodyFields;
        this.methods = methods;
        this.captured = captured;
        this.declaredFields = new LinkedHashSet<>();
        for (NotchField f : headerFields) declaredFields.add(f.getName().str());
        for (NotchField f : bodyFields) declaredFields.add(f.getName().str());
    }

    public record Method(List<Token> params, List<NotchStatement> body) {
        //method template
    }

    public boolean declaresField(String fieldName) {
        return declaredFields.contains(fieldName);
    }

    public String closestDeclaredField(String name) {
        return Text.closest(name, declaredFields, 2);
    }

    public NotchInstance construct(NotchRuntime caller, List<Object> args) {
        if (args.size() != headerFields.size()) {
            throw new IllegalArgumentException(
                    "class %s expects %d constructor argument(s), got %d"
                            .formatted(name, headerFields.size(), args.size()));
        }
        NotchInstance instance = new NotchInstance(this);
        for (int i = 0; i < headerFields.size(); i++) {
            String fieldName = headerFields.get(i).getName().str();
            instance.fields.put(fieldName, args.get(i));
        }
        try (var scope = captured.pushScope()) {
            scope.define("this", instance);
            for (NotchField f : headerFields) {
                String fieldName = f.getName().str();
                scope.define(fieldName, instance.fields.get(fieldName));
            }
            for (NotchField f : bodyFields) {
                String fieldName = f.getName().str();
                Object value = f.getInitializer() == null ? UNDEFINED : captured.evaluate(f.getInitializer());
                instance.fields.put(fieldName, value);
                scope.define(fieldName, value);
            }
        }
        return instance;
    }

    public NotchClosure boundMethod(NotchInstance self, String methodName) {
        Method m = methods.get(methodName);
        if (m == null) return null;
        NotchRuntime methodScope = new NotchRuntime(captured.source, captured);
        methodScope.defineOrUpdate("this", self);
        return new NotchClosure(methodScope, m.params(), null, m.body(), name + "." + methodName);
    }

    @Override
    public String toString() {
        return "<class:" + name + ">";
    }
}
