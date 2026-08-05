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
                .highlight(parser.currentToken())
                .note("the '(' at line %d, column %d is never closed"
                        .formatted(openParen.start().line, openParen.start().column));
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

    public ParseException expectedExpressionAfterRecover() {
        final var diag = diag(ParserError.EP0027)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedIndexExpression() {
        final var diag = diag(ParserError.EP0028)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedExpressionToThrow() {
        final var diag = diag(ParserError.EP0029)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedConditionAfter(String keyword) {
        final var diag = diag(ParserError.EP0030, keyword)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedCountExpressionAfterRepeat() {
        final var diag = diag(ParserError.EP0031)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedExpressionForLoopIterable() {
        final var diag = diag(ParserError.EP0034)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedInitializerExpressionAfterEquals() {
        final var diag = diag(ParserError.EP0032)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedExpressionAfterEquals() {
        final var diag = diag(ParserError.EP0033)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    private Diagnostic identExpected(ParserError code) {
        return diag(code)
                .note("expected an identifier here")
                .highlight(parser.currentToken());
    }

    public ParseException expectedPropertyName() {
        return new ParseException(identExpected(ParserError.EP0035));
    }

    public ParseException expectedClassNameAfterNew() {
        return new ParseException(identExpected(ParserError.EP0036));
    }

    public ParseException expectedAliasAfterAs() {
        return new ParseException(identExpected(ParserError.EP0037));
    }

    public ParseException expectedBindingNameAfterAs() {
        return new ParseException(identExpected(ParserError.EP0038));
    }

    public ParseException expectedParameterName() {
        return new ParseException(identExpected(ParserError.EP0039));
    }

    public ParseException expectedFunctionName() {
        final var diag = identExpected(ParserError.EP0040);
        if (parser.currentToken().type.equals("(")) {
            diag.note("Notch functions are always named");
            diag.note("for an anonymous function use a closure: \\ x -> x + 1");
        }
        return new ParseException(diag);
    }

    public ParseException expectedClassName() {
        return new ParseException(identExpected(ParserError.EP0041));
    }

    public ParseException expectedFieldName() {
        return new ParseException(identExpected(ParserError.EP0042));
    }

    public ParseException expectedLoopItemName() {
        return new ParseException(identExpected(ParserError.EP0043));
    }

    public ParseException expectedLoopIndexName() {
        return new ParseException(identExpected(ParserError.EP0044));
    }

    public ParseException expectedWithAfter(String keyword) {
        final var diag = diag(ParserError.EP0045, keyword)
                .note("expected the identifier %s here".formatted(Text.repr("with")))
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedEndOfBlock(String construct) {
        final var diag = diag(ParserError.EP0046, construct)
                .note(Text.repr("end"))
                .highlight(parser.currentToken());
        // special cases
        if (construct.equals("try block")) {
            diag.note("any 'catch' clauses go before the 'end'");
        } else if (construct.equals("if statement")) {
            diag.note("the trailing 'if' operator needs no 'end': x = 1 if ready else 0");
        }
        return new ParseException(diag);
    }

    public ParseException expectedInAfterLoopVariable() {
        final var diag = diag(ParserError.EP0047)
                .note(Text.repr("in"))
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedTypeNameAfterImport() {
        final var diag = diag(ParserError.EP0048)
                .highlight(parser.currentToken());
        return new ParseException(diag);
    }

    public ParseException expectedTypeAfterColon(String position) {
        final var diag = diag(ParserError.EP0049, position)
                .highlight(parser.currentToken());
        // special case: a value where a type belongs, e.g. 'field x: = 5'
        if (parser.currentToken().type.equals("=")) {
            diag.note("a ':' introduces a type, not a value");
            diag.note("to give a default without a type, drop the ':'");
        }
        return new ParseException(diag);
    }

    private Diagnostic tokenExpected(ParserError code, Object... args) {
        return diag(code, args)
                .note("instead found %s".formatted(Text.repr(parser.currentToken().type)))
                .highlight(parser.currentToken());
    }

    public ParseException expectedCloseBracketForIndex() {
        return new ParseException(tokenExpected(ParserError.EP0050));
    }

    public ParseException expectedOpenParenAfterClassName() {
        return new ParseException(tokenExpected(ParserError.EP0051));
    }

    public ParseException expectedCommaBetweenListElements() {
        return new ParseException(tokenExpected(ParserError.EP0052));
    }

    public ParseException expectedCloseBracketForList() {
        return new ParseException(tokenExpected(ParserError.EP0053));
    }

    public ParseException expectedCloseBraceFor(String literalKind) {
        final var diag = tokenExpected(ParserError.EP0054, literalKind);
        if (literalKind.equals("empty set")) {
            diag.note("the empty set is written '{,}' - the comma is what distinguishes it");
            diag.note("'{}' on its own is the empty map");
        }
        return new ParseException(diag);
    }

    public ParseException expectedArrowBetweenMapKeyAndValue() {
        return new ParseException(tokenExpected(ParserError.EP0055));
    }

    public ParseException expectedCommaBetweenParameters() {
        return new ParseException(tokenExpected(ParserError.EP0056));
    }

    public ParseException expectedArrowAfterClosureParameters() {
        return new ParseException(tokenExpected(ParserError.EP0057));
    }

    public ParseException expectedCloseBraceForClosureBody() {
        return new ParseException(tokenExpected(ParserError.EP0058)
                .note("or drop the braces for an expression body: \\ params -> expression"));
    }

    public ParseException expectedOpenParenAfterFunctionName() {
        return new ParseException(tokenExpected(ParserError.EP0059));
    }

    public ParseException expectedCloseParenFor(String listKind) {
        return new ParseException(tokenExpected(ParserError.EP0060, listKind));
    }
}
