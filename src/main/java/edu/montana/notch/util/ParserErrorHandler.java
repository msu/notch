package edu.montana.notch.util;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.DiagnosticCode;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.errors.ParserError;
import edu.montana.notch.expressions.NotchClosureExpression;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.expressions.NotchIdentifierExpression;

public class ParserErrorHandler {

    private final NotchParser parser;

    public ParserErrorHandler(NotchParser parser) {
        this.parser = parser;
    }

    private static Diagnostic diag(DiagnosticCode code, Object... args) {
        return new Diagnostic().code(code).setTitle(code.title(args));
    }

    public ParseException expectedConditionAfterIfOperator() {
        final var diag = diag(ParserError.EP0001)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedValueAfterElseOperator() {
        final var diag = diag(ParserError.EP0002)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedExpressionAfterOperator(String operatorText) {
        final var diag = diag(ParserError.EP0003, operatorText)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException catchInRecoverExpression() {
        final var diag = diag(ParserError.EP0004)
                .note("use a try/catch block to catch exceptions with side effects")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedExpressionAfterRecoverType() {
        final var diag = diag(ParserError.EP0005)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException unexpectedTokenAfterRecover() {
        final var diag = diag(ParserError.EP0006)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedCommaBetweenArguments() {
        final var diag = diag(ParserError.EP0007)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedCloseParenForArguments() {
        final var diag = diag(ParserError.EP0008)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedCloseParenForGrouping(Token openParen) {
        final var diag = diag(ParserError.EP0009)
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
        final var diag = diag(ParserError.EP0010, parser.currentToken().str())
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedStatement() {
        final var diag = diag(ParserError.EP0011)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException cannotAssignToThisExpression(NotchExpression expr) {
        final var diag = diag(ParserError.EP0012)
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
        final var diag = diag(ParserError.EP0013)
                .note("expected a function, a method call, or an assignment")
                .highlight(expr);

        // special cases
        Token next = parser.currentToken();
        boolean inputFollowsOnSameLine = !next.type.equals("eoi") && next.startLine() == expr.endLine();

        if (expr instanceof NotchIdentifierExpression keywordCandidate && inputFollowsOnSameLine && next.type.equals("ident")) {
            Token afterNext = parser.peekNext();
            if (afterNext.type.equals("=") && afterNext.startLine() == next.endLine()) {
                final var declarationDiag = diag(ParserError.EP0026, keywordCandidate.name())
                        .note("variables are declared by assigning to them")
                        .note("try: " + next.str() + " = ...")
                        .highlight(expr);
                return new ParseException(declarationDiag);
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
        final var diag = diag(ParserError.EP0014)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException rethrowOutsideCatch(Span span) {
        final var diag = diag(ParserError.EP0015)
                .highlight(span);
        return new ParseException(diag);
    }

    public ParseException catchBodyMustStartOnNewLine() {
        final var diag = diag(ParserError.EP0016)
                .note("to bind the exception use 'catch IOException as e'")
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedTimesAfterRepeatCount() {
        final var diag = diag(ParserError.EP0017)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException keywordOutsideLoop(Span span, String keyword) {
        final var diag = diag(ParserError.EP0018, keyword)
                .highlight(span);
        return new ParseException(diag);
    }

    public ParseException keywordOutsideFunction(Span span, String keyword) {
        final var diag = diag(ParserError.EP0019, keyword)
                .highlight(span);
        return new ParseException(diag);
    }

    public ParseException expectedFieldOrFunctionInClassBody() {
        final var diag = diag(ParserError.EP0020)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException cannotAssignToThis(Token varName) {
        final var diag = diag(ParserError.EP0021)
                .note("'this' always refers to the current object and cannot be reassigned")
                .highlight(varName.span());
        return new ParseException(diag);
    }

    public ParseException keywordAsLoopVariable() {
        final var diag = diag(ParserError.EP0022, parser.currentToken().str())
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedConditionalAfterIf(Token ifToken) {
        final var diag = diag(ParserError.EP0023)
                .highlight(ifToken);
        return new ParseException(diag);
    }

    public ParseException expectedParenAfterPrint(Token printKeyword) {
        final var diag = diag(ParserError.EP0024)
                .highlight(parser.currentToken());
        Token next = parser.currentToken();
        if (!next.type.equals("eoi") && next.startLine() == printKeyword.endLine()) {
            diag.note("print requires parentheses around its arguments");
            diag.note("try: print(...)");
        }
        return new ParseException(diag);
    }

    public ParseException expectedCloseParenForPrint() {
        final var diag = diag(ParserError.EP0025)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }
}
