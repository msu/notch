package edu.montana.notch.errors;

import edu.montana.notch.chisel.DiagnosticCode;

public enum ParserError implements DiagnosticCode {

    //List is long and can grow if needed but allows for specificity in otherwise
    // opaque msgs
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
    EP0027("expected an expression after 'recover'"),
    EP0028("expected an index expression"),
    EP0029("expected an expression to throw"),
    EP0030("expected condition after '%s'"),
    EP0031("expected a count expression after 'repeat'"),
    EP0032("expected an initializer expression after '='"),
    EP0033("expected an expression after '='"),
    EP0034("expected an expression for the loop iterable"),
    EP0035("expected a property name"),
    EP0036("expected a class name after 'new'"),
    EP0037("expected an alias after 'as'"),
    EP0038("expected a binding name after 'as'"),
    EP0039("expected a parameter name"),
    EP0040("expected a function name"),
    EP0041("expected a class name"),
    EP0042("expected a field name"),
    EP0043("expected a variable name for the loop item"),
    EP0044("expected a variable name for the loop index"),
    EP0045("expected 'with' after '%s'"),
    EP0046("expected 'end' to close the %s"),
    EP0047("expected 'in' after the loop variable"),
    EP0048("expected a type name after 'import'"),
    EP0049("expected a %s type after ':'"),
    EP0050("expected ']' to close the index expression"),
    EP0051("expected '(' after the class name"),
    EP0052("expected ',' between list elements"),
    EP0053("expected ']' to close the list"),
    EP0054("expected '}' to close the %s"),
    EP0055("expected '->' between a map key and value"),
    EP0056("expected ',' between parameters"),
    EP0057("expected '->' after the closure parameters"),
    EP0058("expected '}' to close the closure body"),
    EP0059("expected '(' after the function name"),
    EP0060("expected ')' to close the %s"),
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
