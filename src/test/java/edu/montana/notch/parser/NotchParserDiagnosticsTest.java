package edu.montana.notch.parser;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.errors.ParserError;
import edu.montana.notch.statements.NotchAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.montana.notch.NotchTestUtils.execDiagnostics;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class NotchParserDiagnosticsTest {

    private static NotchParser parserFor(String src) {
        return new NotchParser(new Source("<test>", src));
    }

    private static List<String> errorsIn(String source) {
        return keysOf(execDiagnostics(source));
    }

    private static List<String> keysOf(List<Diagnostic> diagnostics) {
        return diagnostics.stream().map(NotchParserDiagnosticsTest::key).toList();
    }

    private static String key(Diagnostic diagnostic) {
        if (diagnostic.getTitle() != null) return diagnostic.getTitle();
        return diagnostic.getNotes().isEmpty() ? "(no title, no note)" : diagnostic.getNotes().getFirst();
    }

    @Test
    void expressionPathCollectsSingleDiagnostic() {
        assertEquals(List.of("An expression is required"), errorsIn("arr[]"));
    }

    @Test
    void cleanParseCollectsNoDiagnostics() {
        assertEquals(List.of(), errorsIn("print(2 + 3)"));
    }

    @Test
    void invalidAssignmentTargetReportsAtTheExpression() {
        assertEquals(List.of(ParserError.EP0012.template()), errorsIn("x + 1 = 5"));
    }

    @Test
    void invalidAssignmentTargetCommitsAbsorbedExpressionDiagnostics() {
        assertEquals(List.of("An expression is required", ParserError.EP0012.template()),
                errorsIn("foo()[] = 5"));
    }

    @Test
    void abandonedAssignmentAttemptRollsBackAbsorbedDiagnostics() {
        var parser = parserFor("arr[].size()");
        parser.parseAsStatement();
        assertEquals(List.of("An expression is required"), keysOf(parser.getDiagnostics()));
    }

    @Test
    void indexAssignmentWithMissingIndexReportsOnlyTheMissingIndex() {
        assertEquals(List.of("expected an index expression"), errorsIn("arr[] = 5"));
    }

    @Test
    void incompleteBinaryExpressionRethrownThroughCallStatementPath() {
        assertEquals(List.of("expected expression after '+' operator"), errorsIn("1 +"));
    }

    @Test
    void unterminatedCallRethrownThroughCallStatementPath() {
        assertEquals(List.of(ParserError.EP0008.template()), errorsIn("foo("));
    }

    @Test
    void unterminatedParenRethrownThroughCallStatementPath() {
        assertEquals(List.of(ParserError.EP0009.template()), errorsIn("(1 + 2"));
    }

    @Test
    void unclosedParenPointsAtOnePlaceAndNamesTheOtherInANote() {
        assertEquals(1, excerptCount(execDiagnostics("(1 + 2").getFirst()));
    }

    @Test
    void trailingInputAfterAnExpressionIsNotHighlightedSeparately() {
        assertEquals(1, excerptCount(execDiagnostics("foo 5").getFirst()));
    }

    private static long excerptCount(Diagnostic diagnostic) {
        return diagnostic.render(false).lines().filter(line -> line.contains("--> ")).count();
    }

    @Test
    void brokenRhsInAssignmentStillProducesAssignmentNode() {
        var parser = parserFor("x = 1 +");
        assertInstanceOf(NotchAssignment.class, parser.parse());
    }

    @Test
    void brokenRhsInAssignmentStillReportsTheOperator() {
        var parser = parserFor("x = 1 +");
        parser.parse();
        assertEquals(List.of("expected expression after '+' operator"),
                keysOf(parser.getDiagnostics()));
    }

    @Test
    void declarationKeywordIsReportedAsADeclarationAttempt() {
        assertEquals(List.of(ParserError.EP0026.title("var")), errorsIn("var hello = 5"));
    }

    @Test
    void typeNameDeclarationIsReportedAsADeclarationAttempt() {
        assertEquals(List.of(ParserError.EP0026.title("int")), errorsIn("int x = 5"));
    }

    @Test
    void assignmentOnNextLineIsNotMistakenForADeclaration() {
        assertEquals(List.of(ParserError.EP0013.template()), errorsIn("""
                foo
                bar = 5
                """));
    }

    @Test
    void multiplicativeOperatorIsNamedCorrectly() {
        assertEquals(List.of("expected expression after '*' operator"), errorsIn("x = 1 *"));
    }

    @Test
    void printWithoutParensIsReported() {
        assertEquals(List.of(ParserError.EP0024.template()), errorsIn("print hello"));
    }

    @Test
    void printWithoutAnArgumentIsReported() {
        assertEquals(List.of(ParserError.EP0024.template()), errorsIn("print"));
    }

    @Test
    void printWithItsArgumentOnTheNextLineIsReported() {
        assertEquals(List.of(ParserError.EP0024.template()), errorsIn("""
                print
                hello
                """));
    }

    @Test
    void brokenExpressionInsidePrintReportsExpressionDiagnostic() {
        assertEquals(List.of("expected expression after '+' operator"), errorsIn("print(1 +"));
    }

    @Test
    void absorbedDiagnosticSurvivesTrailingTokens() {
        assertEquals(List.of("An expression is required", ParserError.EP0013.template()),
                errorsIn("arr[] 5"));
    }
}
