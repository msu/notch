package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchClass;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NotchClassDeclaration extends NotchStatement {
    private final Token name;
    private final List<NotchField> headerFields;
    private final List<NotchField> bodyFields;
    private final List<NotchFunctionDeclaration> methods;

    public NotchClassDeclaration(Span span, Token name, List<NotchField> headerFields,
                                 List<NotchField> bodyFields, List<NotchFunctionDeclaration> methods) {
        super(span);
        this.name = name;
        this.headerFields = headerFields;
        this.bodyFields = bodyFields;
        this.methods = methods;
        addChildren(methods);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        NotchRuntime captured = new NotchRuntime(source(), runtime);
        Map<String, NotchClass.Method> methodMap = new LinkedHashMap<>();
        for (NotchFunctionDeclaration m : methods) {
            String methodName = m.name().str();
            NotchClass.Method method = new NotchClass.Method(m.paramTokens(), m.body());
            methodMap.put(methodName, method);
        }
        NotchClass clazz = new NotchClass(name.str(), headerFields, bodyFields, methodMap, captured);
        runtime.defineOrUpdate(name.str(), clazz);
    }
}
