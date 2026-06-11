package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchClosure;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.templates.ast.QualifiedIdent;

import java.util.ArrayList;
import java.util.List;

public class NotchFunctionDeclaration extends NotchStatement {
    private final Token name;
    private final List<NotchField> params;
    private final QualifiedIdent returnType;
    private final List<NotchStatement> body;

    public NotchFunctionDeclaration(Span span, Token name, List<NotchField> params,
                                    QualifiedIdent returnType, List<NotchStatement> body) {
        super(span);
        this.name = name;
        this.params = params;
        this.returnType = returnType;
        this.body = body;
        addChildren(body);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        NotchRuntime captured = new NotchRuntime(source(), runtime);
        List<Token> paramTokens = new ArrayList<>(params.size());
        for (NotchField field : params) {
            paramTokens.add(field.getName());
        }
        NotchClosure closure = new NotchClosure(captured, paramTokens, null, body, name.str());
        runtime.defineOrUpdate(name.str(), closure);
    }

    public Token name() {
        return name;
    }

    public List<Token> paramTokens() {
        List<Token> paramTokens = new ArrayList<>(params.size());
        for (NotchField field : params) {
            paramTokens.add(field.getName());
        }
        return paramTokens;
    }

    public List<NotchStatement> body() {
        return body;
    }
}
