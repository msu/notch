package edu.montana.notch.parser;

import edu.montana.notch.chisel.Diagnostic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.montana.notch.NotchTestUtils.describe;
import static edu.montana.notch.NotchTestUtils.execDiagnostics;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Conditional hints attached to parser diagnostics.
 *
 * <p>ParserErrorHandler attaches 16 notes. Eleven of them sit behind an {@code if}, so the
 * same code is emitted with and without them - the note is a branch, not a property of the
 * code. Those branches are what this file covers, one positive and one negative case each.
 *
 * <p>The other five notes are unconditional links in a builder chain (EP0004:46, EP0012:101,
 * EP0013:113, EP0016:158, EP0021:189). Those the code already implies, so they are
 * deliberately not asserted anywhere.
 *
 * <p>Which code fires is asserted in NotchParserDiagnosticsTest via assertCodes; this file
 * never asserts codes. The one exception lives there rather than here: EP0026's condition
 * changes the code itself (EP0026 instead of EP0013), not just the notes, so its negative
 * case is a code assertion.
 */
class ParserDiagnosticHintTest {

    private static boolean hasNote(List<Diagnostic> diagnostics, String note) {
        return diagnostics.stream()
                .anyMatch(d -> d.getNotes().stream().anyMatch(n -> n.contains(note)));
    }

    // --- EP0009, ParserErrorHandler:80-83 - fires when the offending token is '=' ---

    @Test
    void unclosedGroupEndingInAssignmentSuggestsComparison() {
        var diagnostics = execDiagnostics("(x = 1");
        assertTrue(hasNote(diagnostics, "'=' is assignment, not comparison"),
                "expected the assignment-vs-comparison hint:\n" + describe(diagnostics));
        assertTrue(hasNote(diagnostics, "to compare two values use '=='"),
                "expected the '==' suggestion:\n" + describe(diagnostics));
    }

    @Test
    void unclosedGroupWithoutAssignmentOmitsTheComparisonHint() {
        var diagnostics = execDiagnostics("(1 + 2");
        assertFalse(hasNote(diagnostics, "'=' is assignment, not comparison"),
                "the hint is only for a '=' token:\n" + describe(diagnostics));
    }

    // --- EP0012, ParserErrorHandler:104-107 - fires when the target is a closure ---

    @Test
    void assigningToAnExpressionBodiedClosureExplainsWhy() {
        var diagnostics = execDiagnostics("\\ n -> n = 5");
        assertTrue(hasNote(diagnostics, "an expression-bodied closure cannot contain an assignment"),
                "expected the closure-target hint:\n" + describe(diagnostics));
        assertTrue(hasNote(diagnostics, "use a block body for statements"),
                "expected the block-body suggestion:\n" + describe(diagnostics));
    }

    @Test
    void assigningToAnOrdinaryExpressionOmitsTheClosureHint() {
        var diagnostics = execDiagnostics("x + 1 = 5");
        assertFalse(hasNote(diagnostics, "an expression-bodied closure cannot contain an assignment"),
                "the hint is only for closure targets:\n" + describe(diagnostics));
    }

    // --- EP0013, ParserErrorHandler:131-136 - inReturnableContext / inClosureBody ---

    @Test
    void bareExpressionInClosureBlockSuggestsReturnAndExpressionBody() {
        var diagnostics = execDiagnostics("\\ n -> { n + 1 }");
        assertTrue(hasNote(diagnostics, "use 'return'"),
                "expected the return hint:\n" + describe(diagnostics));
        assertTrue(hasNote(diagnostics, "expression body"),
                "expected the expression-body hint:\n" + describe(diagnostics));
    }

    @Test
    void bareExpressionInFunctionBodySuggestsReturnButNotBraces() {
        // a function body is returnable but is not a closure body, so only one branch fires
        var diagnostics = execDiagnostics("function f(n) n + 1 end");
        assertTrue(hasNote(diagnostics, "use 'return'"),
                "expected the return hint:\n" + describe(diagnostics));
        assertFalse(hasNote(diagnostics, "expression body"),
                "a function body should not get the closure-only hint:\n" + describe(diagnostics));
    }

    @Test
    void properReturnInBlockProducesNoDiagnostic() {
        var diagnostics = execDiagnostics("\\ n -> { return n + 1 }");
        assertTrue(diagnostics.isEmpty(),
                "a block closure using return should parse cleanly:\n" + describe(diagnostics));
    }

    @Test
    void bareExpressionAtTopLevelHasNoReturnHint() {
        // `return` is not valid at the top level, so no return hint there.
        var diagnostics = execDiagnostics("x = 1\nx + 1");
        assertFalse(hasNote(diagnostics, "use 'return'"),
                "top-level bare expression should not suggest return:\n" + describe(diagnostics));
    }

    // --- EP0013, ParserErrorHandler:137-140 - input follows on the same line ---

    @Test
    void trailingInputOnTheSameLineIsCalledOut() {
        var diagnostics = execDiagnostics("foo 5");
        assertTrue(hasNote(diagnostics, "unexpected input after this expression"),
                "expected the trailing-input hint:\n" + describe(diagnostics));
    }

    @Test
    void inputOnTheNextLineIsNotTrailingInput() {
        // 'bar = 5' is its own statement, so nothing trails 'foo'
        var diagnostics = execDiagnostics("foo\nbar = 5");
        assertFalse(hasNote(diagnostics, "unexpected input after this expression"),
                "a following line is not trailing input:\n" + describe(diagnostics));
    }

    // --- EP0024, ParserErrorHandler:210-213 - a token follows 'print' on the same line ---

    @Test
    void printWithoutParensSuggestsWrappingTheArgument() {
        var diagnostics = execDiagnostics("print hello");
        assertTrue(hasNote(diagnostics, "print requires parentheses around its arguments"),
                "expected the parentheses hint:\n" + describe(diagnostics));
        assertTrue(hasNote(diagnostics, "try: print(...)"),
                "expected the rewrite suggestion:\n" + describe(diagnostics));
    }

    @Test
    void printWithoutAnArgumentOmitsTheSuggestion() {
        var diagnostics = execDiagnostics("print");
        assertFalse(hasNote(diagnostics, "try: print("),
                "no suggestion without an argument on the line:\n" + describe(diagnostics));
    }

    @Test
    void printWithItsArgumentOnTheNextLineOmitsTheSuggestion() {
        // the argument candidate must be on the same line as 'print';
        // 'hello' here is plausibly its own (broken) statement.
        var diagnostics = execDiagnostics("print\nhello");
        assertFalse(hasNote(diagnostics, "try: print("),
                "no suggestion across lines:\n" + describe(diagnostics));
    }

    // --- EP0026, ParserErrorHandler:123-126 - the note names the offending token ---

    @Test
    void declarationKeywordHintNamesTheIdentifier() {
        var diagnostics = execDiagnostics("var hello = 5");
        assertTrue(hasNote(diagnostics, "variables are declared by assigning to them"),
                "expected the declaration explanation:\n" + describe(diagnostics));
        assertTrue(hasNote(diagnostics, "try: hello = ..."),
                "the suggestion should name 'hello':\n" + describe(diagnostics));
    }

    @Test
    void typedDeclarationHintNamesTheIdentifier() {
        var diagnostics = execDiagnostics("int x = 5");
        assertTrue(hasNote(diagnostics, "try: x = ..."),
                "the suggestion should name 'x':\n" + describe(diagnostics));
    }
}
