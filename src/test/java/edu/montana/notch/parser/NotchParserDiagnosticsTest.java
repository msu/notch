package edu.montana.notch.parser;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.DiagnosticCode;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.errors.ParserError;
import edu.montana.notch.statements.NotchAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.montana.notch.NotchTestUtils.UNCODED;
import static edu.montana.notch.NotchTestUtils.assertCodes;
import static edu.montana.notch.NotchTestUtils.describe;
import static edu.montana.notch.NotchTestUtils.execDiagnostics;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Which diagnostics parse() produces for input the grammar disagrees about.
//
// Every test states the complete code list via assertCodes, so "no other diagnostic
// appeared" is structural rather than a list of codes that must not fire.
//
// Diagnostic *text* is asserted here in only two cases, both unavoidable: the argument
// filled into a templated code, and the identity of a diagnostic that has no code yet.
// Conditional hint notes live in ParserDiagnosticHintTest.
class NotchParserDiagnosticsTest {

    private static NotchParser parserFor(String src) {
        return new NotchParser(new Source("<test>", src));
    }

    /**
     * Diagnostics carrying {@code code} whose interpolated title names {@code argument}.
     *
     * <p>For templated codes only. EP0003 is {@code "expected expression after '%s' operator"},
     * so the code alone cannot tell {@code +} from {@code *} - and telling those apart is the
     * whole point of {@code multiplicativeOperatorIsNamedCorrectly}. The enum gives identity;
     * the string gives only the {@code %s}.
     */
    private static long countCodeWithArgument(List<Diagnostic> diagnostics,
                                              DiagnosticCode code,
                                              String argument) {
        return diagnostics.stream()
                .filter(d -> d.getCode() == code)
                .filter(d -> d.getTitle() != null && d.getTitle().contains(argument))
                .count();
    }

    /**
     * Identifies a diagnostic that carries no code, by the note serving as its message.
     *
     * <p>Only used where {@code assertCodes} reads {@code UNCODED}. Around 40 parser sites
     * still report through {@code requireExpression} / {@code require} / {@code requireIdent},
     * which build a bare {@code .note(message)} with no title and no code, so the note text is
     * the only thing telling one apart from another. Allocating codes for those sites retires
     * this helper entirely - see local/futureFixs/error-index-plan.md.
     */
    private static long countNote(List<Diagnostic> diagnostics, String note) {
        return diagnostics.stream()
                .filter(d -> d.getNotes().stream().anyMatch(n -> n.contains(note)))
                .count();
    }

    @Test
    void expressionPathCollectsSingleDiagnostic() {
        // 'arr[]' parses fully as an expression; requireExpression inside
        // parseIndexOperation absorbs the missing index and records one diagnostic.
        var diagnostics = execDiagnostics("arr[]");
        assertCodes(diagnostics, UNCODED);
        assertEquals(1, countNote(diagnostics, "An expression is required"),
                "expected the missing-index diagnostic:\n" + describe(diagnostics));
    }

    @Test
    void cleanParseCollectsNoDiagnostics() {
        assertCodes(execDiagnostics("print(2 + 3)"));
    }

    @Test
    void invalidAssignmentTargetReportsAtTheExpression() {
        // 'x + 1 = 5' parses 'x + 1' as an expression and then hits '='; the
        // call-statement attempt commits an invalid-assignment-target diagnostic
        // highlighting the expression instead of rolling back to the generic
        // EP0011 at column 1.
        var diagnostics = execDiagnostics("x + 1 = 5");
        assertCodes(diagnostics, ParserError.EP0012);
        var rendered = diagnostics.getFirst().render(false);
        assertTrue(rendered.contains("^^^^^"),
                "caret should span the full 'x + 1' target, got:\n" + rendered);
    }

    @Test
    void invalidAssignmentTargetCommitsAbsorbedExpressionDiagnostics() {
        // 'foo()[] = 5' has two real problems: the missing index inside '[]'
        // (absorbed by requireExpression while parsing the expression) and the
        // non-assignable target itself. The call-statement attempt commits, so
        // both are reported rather than collapsing to the generic fallback.
        var diagnostics = execDiagnostics("foo()[] = 5");
        assertCodes(diagnostics, UNCODED, ParserError.EP0012);
        assertEquals(1, countNote(diagnostics, "An expression is required"),
                "expected the missing-index diagnostic:\n" + describe(diagnostics));
    }

    @Test
    void abandonedAssignmentAttemptRollsBackAbsorbedDiagnostics() {
        // 'arr[].size()' is not an assignment: the speculative assignment-target
        // parse absorbs a missing-index diagnostic and then rewinds on the
        // missing '='. That absorbed diagnostic must be rolled back with the
        // tokens, leaving only the one from the committed call-statement parse.
        //
        // Must drive parseAsStatement() directly: through parse() this input succeeds
        // as an expression (NotchMethodInvocationExpression) and returns before the
        // statement route runs, so the speculation under test never happens. The
        // diagnostic count is 1 either way, so going through exec() would keep passing
        // while testing nothing.
        var parser = parserFor("arr[].size()");
        parser.parseAsStatement();
        assertCodes(parser.getDiagnostics(), UNCODED);
        assertEquals(1, countNote(parser.getDiagnostics(), "An expression is required"),
                "expected the missing-index diagnostic:\n" + describe(parser.getDiagnostics()));
    }

    @Test
    void indexAssignmentWithMissingIndexReportsOnlyTheMissingIndex() {
        // 'arr[] = 5' is a valid index-assignment statement shape, so the only
        // problem is the missing index expression inside '[]'. EP0012 no longer applies.
        var diagnostics = execDiagnostics("arr[] = 5");
        assertCodes(diagnostics, UNCODED);
        assertEquals(1, countNote(diagnostics, "expected an index expression"),
                "expected the missing-index diagnostic:\n" + describe(diagnostics));
    }

    @Test
    void incompleteBinaryExpressionRethrownThroughCallStatementPath() {
        // '1 +' fails as an expression; the statement pass re-encounters the same
        // failure inside parseCallStatement, which rethrows because the expression
        // parse advanced from its mark. The specific operator diagnostic is
        // committed rather than the generic fallback.
        var diagnostics = execDiagnostics("1 +");
        assertCodes(diagnostics, ParserError.EP0003);
        assertEquals(1, countCodeWithArgument(diagnostics, ParserError.EP0003, "'+'"),
                "expected the operator diagnostic to name '+':\n" + describe(diagnostics));
    }

    @Test
    void unterminatedCallRethrownThroughCallStatementPath() {
        assertCodes(execDiagnostics("foo("), ParserError.EP0008);
    }

    @Test
    void unterminatedParenRethrownThroughCallStatementPath() {
        assertCodes(execDiagnostics("(1 + 2"), ParserError.EP0009);
    }

    @Test
    void brokenRhsInAssignmentStillProducesAssignmentNode() {
        // 'x = 1 +' fails pass 1 (not an expression), and in pass 2 the
        // assignment parser's requireExpression absorbs the broken RHS into a
        // NotchErrorExpression: the diagnostic survives AND a real statement
        // node is still produced.
        //
        // Must hold the parser: Notch.run throws on parse errors and never returns the
        // node, so the "a statement node is still produced" half is only observable here.
        var parser = parserFor("x = 1 +");
        var result = parser.parse();
        assertInstanceOf(NotchAssignment.class, result,
                "expected a NotchAssignment despite the broken RHS, got: " + result);
        assertCodes(parser.getDiagnostics(), ParserError.EP0003);
        assertEquals(1, countCodeWithArgument(parser.getDiagnostics(), ParserError.EP0003, "'+'"),
                "expected the operator diagnostic to name '+':\n" + describe(parser.getDiagnostics()));
    }

    @Test
    void declarationKeywordIsReportedAsADeclarationAttempt() {
        // 'var' parses as a lone identifier; the trailing 'ident =' pattern
        // identifies a declaration-keyword attempt rather than a bare expression.
        assertCodes(execDiagnostics("var hello = 5"), ParserError.EP0026);
    }

    @Test
    void typeNameDeclarationIsReportedAsADeclarationAttempt() {
        // the special case keys on token shape rather than a list of known keywords,
        // so a Java-style type name reaches it too
        assertCodes(execDiagnostics("int x = 5"), ParserError.EP0026);
    }

    @Test
    void assignmentOnNextLineIsNotMistakenForADeclaration() {
        // 'foo' is its own broken statement; 'bar = 5' on the next line is a valid
        // assignment. This is the one special-case negative that belongs here rather
        // than in ParserDiagnosticHintTest: EP0026's condition changes which code
        // fires, so asserting the full list is what rules the special case out.
        assertCodes(execDiagnostics("foo\nbar = 5"), ParserError.EP0013);
    }

    @Test
    void multiplicativeOperatorIsNamedCorrectly() {
        // regression: the multiplicative branch used to report '+' for '*'
        var diagnostics = execDiagnostics("x = 1 *");
        assertCodes(diagnostics, ParserError.EP0003);
        assertEquals(1, countCodeWithArgument(diagnostics, ParserError.EP0003, "'*'"),
                "expected the operator diagnostic to name '*':\n" + describe(diagnostics));
        assertEquals(0, countCodeWithArgument(diagnostics, ParserError.EP0003, "'+'"),
                "the '+' wording must not appear for '*':\n" + describe(diagnostics));
    }

    @Test
    void printWithoutParensIsReported() {
        // EP0024 is emitted whether or not an argument follows on the same line; the
        // suggestion note that distinguishes those cases is in ParserDiagnosticHintTest.
        assertCodes(execDiagnostics("print hello"), ParserError.EP0024);
        assertCodes(execDiagnostics("print"), ParserError.EP0024);
        assertCodes(execDiagnostics("print\nhello"), ParserError.EP0024);
    }

    @Test
    void brokenExpressionInsidePrintReportsExpressionDiagnostic() {
        var diagnostics = execDiagnostics("print(1 +");
        assertCodes(diagnostics, ParserError.EP0003);
        assertEquals(1, countCodeWithArgument(diagnostics, ParserError.EP0003, "'+'"),
                "expected the operator diagnostic to name '+':\n" + describe(diagnostics));
    }

    @Test
    void absorbedDiagnosticSurvivesTrailingTokens() {
        // 'arr[] 5' — pass 1 parses 'arr[]' and requireExpression records the
        // missing-index diagnostic, but the trailing '5' rejects expression
        // mode and rollbackErrors discards it. Pass 2 re-records it inside
        // parseCallStatement, which commits to the expression instead of
        // rolling back: the absorbed diagnostic survives, and EP0013 replaces
        // the generic EP0011 fallback.
        var diagnostics = execDiagnostics("arr[] 5");
        assertCodes(diagnostics, UNCODED, ParserError.EP0013);
        assertEquals(1, countNote(diagnostics, "An expression is required"),
                "the missing-index diagnostic should survive:\n" + describe(diagnostics));
    }
}
