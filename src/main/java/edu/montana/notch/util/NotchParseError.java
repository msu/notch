package edu.montana.notch.util;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.expressions.NotchClosureExpression;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.expressions.NotchIdentifier;

public class NotchParseError {

    private final NotchParser parser;

    public NotchParseError(NotchParser parser) {
        this.parser = parser;
    }

    public ParseException expectedConditionAfterIfOperator() {
        // default case
        final var diag = new Diagnostic()
                .note("expected condition after 'if' operator")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedValueAfterElseOperator() {
        // default case
        final var diag = new Diagnostic()
                .note("expected value after 'else' in 'if' expression")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedExpressionAfterOperator(String operatorText) {
        // default case
        final var diag = new Diagnostic()
                .note("expected expression after '%s' operator".formatted(operatorText))
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException catchInRecoverExpression() {
        // default case
        final var diag = new Diagnostic()
                .note("'catch' is not allowed in a recover expression")
                .note("use a try/catch block to catch exceptions with side effects")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedExpressionAfterRecoverType() {
        // default case
        final var diag = new Diagnostic()
                .note("expected expression after recover type")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException unexpectedTokenAfterRecover() {
        // default case
        final var diag = new Diagnostic()
                .note("unexpected token after recover: expected 'recover' or end of line")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedCommaBetweenArguments() {
        // default case
        final var diag = new Diagnostic()
                .note("Expected ','")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedCloseParenForArguments() {
        // default case
        final var diag = new Diagnostic()
                .note("Expected ')'")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedCloseParenForGrouping(Token openParen) {
        // default case
        final var diag = new Diagnostic()
                .note("Expected a closing parenthesis")
                .highlight(openParen)
                .highlight(parser.currentToken());
        // special case
        if (parser.currentToken().type.equals("=")) {
            diag.note("'=' is assignment, not comparison");
            diag.note("to compare two values use '=='");
        }
        return new ParseException(diag);
    }

    public ParseException keywordAsPropertyName() {
        // default case
        final var diag = new Diagnostic()
                .note("'" + parser.currentToken().str() + "' is a keyword and cannot be used as a property name")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedStatement() {
        // default case
        final var diag = new Diagnostic()
                .note("expected a statement")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException cannotAssignToThisExpression(NotchExpression expr) {
        // default case
        final var diag = new Diagnostic()
                .note("cannot assign to this expression")
                .note("the target of an assignment must be a variable, a property, or an indexed element")
                .highlight(expr);
        // special cases
        if (expr instanceof NotchClosureExpression) {
            diag.note("an expression-bodied closure cannot contain an assignment");
            diag.note("use a block body for statements: \\ -> { ... }");
        }
        return new ParseException(diag);
    }

    public ParseException cannotBeUsedAsAStatement(NotchExpression expr) {
        // default case
        final var diag = new Diagnostic()
                .note("this expression cannot be used as a statement")
                .note("expected a function or method call, or an assignment")
                .highlight(expr);

        // special cases
        Token next = parser.currentToken();
        boolean inputFollowsOnSameLine = !next.type.equals("eoi") && next.startLine() == expr.endLine();

        if (expr instanceof NotchIdentifier keywordCandidate && inputFollowsOnSameLine && next.type.equals("ident")) {
            Token afterNext = parser.peekNext();
            if (afterNext.type.equals("=") && afterNext.startLine() == next.endLine()) {
                diag.note("'" + keywordCandidate.name() + "' is not a keyword in Notch");
                diag.note("variables are declared by assigning to them");
                diag.note("try: " + next.str() + " = ...");
                return new ParseException(diag);
            }
        }

        if (parser.inReturnableContext()) {
            diag.note("to produce a value here, use 'return' before the expression");
        }
        if (parser.inClosureBody()) {
            diag.note("or drop the braces for an expression body: \\ params -> expression");
        }
        if (inputFollowsOnSameLine) {
            diag.note("unexpected input after this expression");
            diag.highlight(next);
        }
        return new ParseException(diag);
    }

    public ParseException expectedExpressionAfterThrow() {
        // default case
        final var diag = new Diagnostic()
                .note("expected an expression after 'throw' on the same line")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException rethrowOutsideCatch(Span span) {
        // default case
        final var diag = new Diagnostic()
                .note("'rethrow' outside a catch")
                .highlight(span);
        return new ParseException(diag);
    }

    public ParseException catchBodyMustStartOnNewLine() {
        // default case
        final var diag = new Diagnostic()
                .note("catch body must start on a new line")
                .note("to bind the exception use 'catch IOException as e'")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedTimesAfterRepeatCount() {
        // default case
        final var diag = new Diagnostic()
                .note("expected 'times' after count expression in 'repeat'")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException keywordOutsideLoop(Span span, String keyword) {
        // default case
        final var diag = new Diagnostic()
                .note("'" + keyword + "' outside a loop")
                .highlight(span);
        return new ParseException(diag);
    }

    public ParseException keywordOutsideFunction(Span span, String keyword) {
        // default case
        final var diag = new Diagnostic()
                .note("'" + keyword + "' outside a function")
                .highlight(span);
        return new ParseException(diag);
    }

    public ParseException expectedFieldOrFunctionInClassBody() {
        // default case
        final var diag = new Diagnostic()
                .note("expected a 'field' or 'function' declaration in the class body")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException cannotAssignToThis(Token varName) {
        // default case
        final var diag = new Diagnostic()
                .note("cannot assign to 'this' it always refers to the current object and can't be reassigned")
                .highlight(varName.span());
        return new ParseException(diag);
    }

    public ParseException keywordAsLoopVariable() {
        // default case
        final var diag = new Diagnostic()
                .note("'" + parser.currentToken().str() + "' is a keyword and cannot be used as a loop variable name")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedConditionalAfterIf(Token ifToken) {
        // default case
        final var diag = new Diagnostic()
                .note("expected a conditional expression after 'if'")
                .highlight(ifToken);
        return new ParseException(diag);
    }

    public ParseException expectedParenAfterPrint(Token printKeyword) {
        // default case
        final var diag = new Diagnostic()
                .note("expected '(' after 'print'")
                .highlight(parser.currentToken());
        Token next = parser.currentToken();
        if (!next.type.equals("eoi") && next.startLine() == printKeyword.endLine()) {
            diag.note("print requires parentheses around its arguments");
            diag.note("try: print(...)");
        }
        return new ParseException(diag);
    }

    public ParseException expectedCloseParenForPrint() {
        // default case
        final var diag = new Diagnostic()
                .note("expected ')' to close the print arguments")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }
}
