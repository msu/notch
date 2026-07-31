package edu.montana.notch.parser;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.errors.ParserError;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.execDiagnostics;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosticRenderShapeTest {

    private static String renderFirstDiagnostic(String source) {
        return execDiagnostics(source).getFirst().render(false);
    }

    private static long linesContaining(String rendered, String fragment) {
        return rendered.lines().filter(line -> line.contains(fragment)).count();
    }

    private static boolean excerptShows(String rendered, String sourceText) {
        return rendered.lines().anyMatch(line -> line.contains("| " + sourceText));
    }

    private static boolean sameArrowIndent(String first, String second) {
        return indentOfArrow(first) == indentOfArrow(second);
    }

    private static int indentOfArrow(String rendered) {
        return rendered.lines()
                .filter(line -> line.contains("--> "))
                .map(line -> line.length() - line.stripLeading().length())
                .findFirst()
                .orElseThrow(() -> new AssertionError("no '-->' line in:\n" + rendered));
    }

    @Test
    void codedDiagnosticRendersExactlyOneCodedHeader() {
        var rendered = renderFirstDiagnostic("x + 1 = 5");
        assertEquals(1, linesContaining(rendered, "ERROR[EP0012]:"),
                "expected exactly one coded header line, got:\n" + rendered);
    }

    @Test
    void primaryMessageAppearsExactlyOnceInTheRender() {
        var rendered = renderFirstDiagnostic("x + 1 = 5");
        assertEquals(1, linesContaining(rendered, ParserError.EP0012.template()),
                "primary message must appear once, in the header only. Got:\n" + rendered);
    }

    @Test
    void interpolatedTitleRendersTheArgumentNotThePlaceholder() {
        var rendered = renderFirstDiagnostic("break");
        assertEquals(1, linesContaining(rendered, "ERROR[EP0018]: 'break' outside a loop"),
                "expected the keyword inserted into the title, got:\n" + rendered);
    }

    @Test
    void uncodedDiagnosticWithATitleRenders() {
        var rendered = new Diagnostic().setTitle("failed to execute statement").render(false);
        assertEquals(" ERROR: failed to execute statement\n", rendered);
    }

    @Test
    void uncodedUntitledDiagnosticRendersNoHeaderAtAll() {
        var rendered = new Diagnostic().note("expected a statement").render(false);
        assertFalse(rendered.contains("ERROR"),
                "an uncoded, untitled diagnostic must emit no header, got:\n" + rendered);
    }

    @Test
    void codeWithoutATitleStillRendersItsCode() {
        var rendered = new Diagnostic().code(ParserError.EP0001).render(false);
        assertEquals(" ERROR[EP0001]\n", rendered);
    }

    @Test
    void diagnosticAtARealTokenShowsSourceAndACaret() {
        var rendered = renderFirstDiagnostic("x + 1 = 5");
        assertTrue(excerptShows(rendered, "x + 1 = 5"),
                "excerpt should display the offending line, got:\n" + rendered);
        assertTrue(rendered.contains("^^^^^"),
                "caret should span the full 'x + 1' target, got:\n" + rendered);
    }

    @Test
    void diagnosticAtEndOfInputShowsSourceAndCaret() {
        var rendered = renderFirstDiagnostic("1 +");
        assertTrue(excerptShows(rendered, "1 +"),
                "excerpt should display the source line, got:\n" + rendered);
        assertTrue(rendered.contains("^"),
                "excerpt should carry a caret at the failure position, got:\n" + rendered);
    }

    @Test
    void unterminatedCallShowsSource() {
        var rendered = renderFirstDiagnostic("foo(");
        assertTrue(excerptShows(rendered, "foo("),
                "excerpt should display the source line, got:\n" + rendered);
    }

    @Test
    void endOfInputDiagnosticUsesTheSameGutterWidth() {
        var diag1 = renderFirstDiagnostic("x + 1 = 5");
        var diag2 = renderFirstDiagnostic("1 +");
        assertTrue(sameArrowIndent(diag1, diag2),
                () -> "an end-of-input diagnostic should not widen the gutter instead got "
                        + indentOfArrow(diag1)
                        + " and "
                        + indentOfArrow(diag2)
                        + ":\n" + diag2);
    }

    //TODO: Fix Shape of diagnostic
    @Test
    void endOfInputHighlightDoesNotDistortSiblingExcerpt() {
        var rendered = renderFirstDiagnostic("(1 + 2");
        assertTrue(rendered.contains("  1 | (1 + 2"),
                "the real excerpt should render flush, got:\n" + rendered);
    }
}
