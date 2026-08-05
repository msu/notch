package edu.montana.notch.parser;

import edu.montana.notch.chisel.Diagnostic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.montana.notch.NotchTestUtils.describe;
import static edu.montana.notch.NotchTestUtils.execDiagnostics;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserDiagnosticHintTest {

    private static boolean hasNote(List<Diagnostic> diagnostics, String note) {
        for (Diagnostic diagnostic : diagnostics) {
            for (String candidate : diagnostic.getNotes()) {
                if (candidate.contains(note)) return true;
            }
        }
        return false;
    }

    @Test
    void unclosedGroupNamesWhereTheParenWasOpened() {
        var diagnostics = execDiagnostics("(1 +\n2\n3\n4\n5");
        assertTrue(hasNote(diagnostics, "the '(' at line 1, column 1 is never closed"),
                "expected the note to locate the unclosed paren:\n" + describe(diagnostics));
    }

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
        var diagnostics = execDiagnostics("""
                x = 1
                x + 1
                """);
        assertFalse(hasNote(diagnostics, "use 'return'"),
                "top-level bare expression should not suggest return:\n" + describe(diagnostics));
    }

    @Test
    void trailingInputOnTheSameLineIsCalledOut() {
        var diagnostics = execDiagnostics("foo 5");
        assertTrue(hasNote(diagnostics, "unexpected input after this expression"),
                "expected the trailing-input hint:\n" + describe(diagnostics));
    }

    @Test
    void inputOnTheNextLineIsNotTrailingInput() {
        var diagnostics = execDiagnostics("""
                foo
                bar = 5
                """);
        assertFalse(hasNote(diagnostics, "unexpected input after this expression"),
                "a following line is not trailing input:\n" + describe(diagnostics));
    }

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
        var diagnostics = execDiagnostics("""
                print
                hello
                """);
        assertFalse(hasNote(diagnostics, "try: print("),
                "no suggestion across lines:\n" + describe(diagnostics));
    }

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
