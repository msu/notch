package edu.montana.notch.errors;

import edu.montana.notch.chisel.DiagnosticCode;

public enum ParserError implements DiagnosticCode {

    EP0001("expected condition after 'if' operator"),
    EP0002("expected value after 'else' in 'if' expression"),
    EP0003("expected expression after '%s' operator"),
    EP0004("'catch' is not allowed in a recover expression"),
    EP0005("expected expression after recover type"),
    EP0006("unexpected token after recover: expected 'recover' or end of line"),
    EP0007("expected ',' between arguments"),
    EP0008("expected ')' to close the argument list"),
    EP0009("expected a closing parenthesis"),
    EP0010("'%s' is a keyword and cannot be used as a property name"),
    EP0011("expected a statement"),
    EP0012("cannot assign to this expression"),
    EP0013("this expression cannot be used as a statement"),
    EP0014("expected an expression after 'throw' on the same line"),
    EP0015("'rethrow' outside a catch"),
    EP0016("catch body must start on a new line"),
    EP0017("expected 'times' after count expression in 'repeat'"),
    EP0018("'%s' outside a loop"),
    EP0019("'%s' outside a function"),
    EP0020("expected a 'field' or 'function' declaration in the class body"),
    EP0021("cannot assign to 'this'"),
    EP0022("'%s' is a keyword and cannot be used as a loop variable name"),
    EP0023("expected a conditional expression after 'if'"),
    EP0024("expected '(' after 'print'"),
    EP0025("expected ')' to close the print arguments"),
    EP0026("'%s' is not a keyword in Notch"),
    ;

    private final String template;

    ParserError(String template) {
        this.template = template;
    }

    @Override
    public String id() {
        return name();
    }

    @Override
    public String template() {
        return template;
    }
}
