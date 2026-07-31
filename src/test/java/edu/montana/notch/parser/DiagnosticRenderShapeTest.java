package edu.montana.notch.parser;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.errors.ParserError;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.execDiagnostics;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DiagnosticRenderShapeTest {

    private static String renderFirstDiagnostic(String source) {
        return execDiagnostics(source).getFirst().render(false);
    }

    private static long linesContaining(String rendered, String fragment) {
        return rendered.lines().filter(line -> line.contains(fragment)).count();
    }

    @Test
    void codedDiagnosticRendersExactlyOneCodedHeader() {
        var rendered = renderFirstDiagnostic("x + 1 = 5");
        assertEquals(1, linesContaining(rendered, "ERROR[EP0012]:"),
                "expected exactly one coded header line, got:\n" + rendered);
    }

    @Test
    void primaryMessageAppearsExactlyOnceInTheRender() {
        // The migration moved each primary message from a note into the title. If a
        // message is ever left in both places this count becomes 2 and every
        // line-counting assertion elsewhere silently doubles.
        var rendered = renderFirstDiagnostic("x + 1 = 5");
        assertEquals(1, linesContaining(rendered, ParserError.EP0012.template()),
                "primary message must appear once, in the header only. Got:\n" + rendered);
    }

    @Test
    void interpolatedTitleRendersTheArgumentNotThePlaceholder() {
        var rendered = renderFirstDiagnostic("break");
        assertEquals(1, linesContaining(rendered, "ERROR[EP0018]: 'break' outside a loop"),
                "expected the keyword interpolated into the title, got:\n" + rendered);
        assertFalse(rendered.contains("%s"), "template placeholder leaked into output:\n" + rendered);
    }

    @Test
    void uncodedDiagnosticWithATitleRendersExactlyAsBefore() {
        // The legacy path: no code, title only. Byte-for-byte what it was before codes.
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
        // Nothing in the parser does this - the diag(...) factory always sets both - but
        // code() is public, and a swallowed code is harder to notice than a bare header.
        var rendered = new Diagnostic().code(ParserError.EP0001).render(false);
        assertEquals(" ERROR[EP0001]\n", rendered);
    }
}
