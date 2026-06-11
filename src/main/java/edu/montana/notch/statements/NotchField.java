package edu.montana.notch.statements;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.templates.ast.QualifiedIdent;

public class NotchField {
    private final Token name;
    private final QualifiedIdent type;
    private final NotchExpression initializer;

    public NotchField(Token name, QualifiedIdent type, NotchExpression initializer) {
        this.name = name;
        this.type = type;
        this.initializer = initializer;
    }

    public Token getName() {
        return name;
    }

    public QualifiedIdent getType() {
        return type;
    }

    public NotchExpression getInitializer() {
        return initializer;
    }
}
