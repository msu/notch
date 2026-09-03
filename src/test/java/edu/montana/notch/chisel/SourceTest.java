package edu.montana.notch.chisel;

import edu.montana.notch.util.Text;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SourceTest {

    // NOTE: Tests locate any errors in positions;
    // parser/DiagnosticRenderShapeTest locates errors in what a user sees.

    private static Source source(String content) {
        return new Source("<t>", content);
    }

    @Test
    void linesReturnsTheRequestedSingleLine() {
        var threeLines = source("a\nbb\nccc");
        assertEquals(List.of("a"), threeLines.lines(1, 1));
        assertEquals(List.of("bb"), threeLines.lines(2, 2));
        assertEquals(List.of("ccc"), threeLines.lines(3, 3));
    }

    @Test
    void linesReturnsInclusiveRanges() {
        var threeLines = source("a\nbb\nccc");
        assertEquals(List.of("a", "bb"), threeLines.lines(1, 2));
        assertEquals(List.of("a", "bb", "ccc"), threeLines.lines(1, 3));
    }

    @Test
    void linesIgnoresASingleTrailingNewline() {
        var trailingNewline = source("1 +\n");
        assertEquals(List.of("1 +"), trailingNewline.lines(1, 1));
        assertTrue(trailingNewline.lines(2, 2).isEmpty(),
                "a trailing newline does not open a second line");
    }

    @Test
    void eoiPointsJustPastTheLastCharacter() {
        assertEquals("1:4 (3)", source("1 +").eoi.span().start().display(),
                "just past '+', which ends at column 3");
    }

    @Test
    void eoiStaysOnTheLastRenderedLine() {
        assertEquals("1:4 (4)", source("1 +\n").eoi.span().start().display(),
                "line must not advance to line 2 since index tracks end of input");
    }

    @Test
    void eoiOnMultipleLinesLandsAtTheEndOfTheLastLine() {
        assertEquals("2:3 (4)", source("a\nbb").eoi.span().start().display(),
                "just past the second 'b'");
    }

    @Test
    void eoiOnEmptyContentIsTheStartOfLineOne() {
        assertEquals("1:1 (0)", source("").eoi.span().start().display());
    }

    @Test
    void eoiIsExactlyOneColumnWide() {
        var eoi = source("1 +").eoi.span();
        assertEquals(1, eoi.end().column - eoi.start().column, "caret width");
        assertEquals(1, eoi.end().index - eoi.start().index, "index width");
        assertEquals(eoi.start().line, eoi.end().line, "should not straddle lines");
    }

    @Test
    void eoiDoesNotCountCarriageReturnAsAColumn() {
        assertEquals("1:4 (5)", source("1 +\r\n").eoi.span().start().display(),
                "the rendered line is \"1 +\", so the caret belongs at column 4");
    }
}
