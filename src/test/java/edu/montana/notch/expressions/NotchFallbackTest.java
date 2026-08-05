package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchFallbackTest {

    @Test
    void fallsBackOnNull() {
        assertEquals("def", eval("null ?: 'def'"));
    }

    @Test
    void fallsBackOnUndefinedVariable() {
        assertEquals("def", eval("missing ?: 'def'"));
    }

    @Test
    void keepsAFalsyNonNullPrimary() {
        assertEquals(false, eval("false ?: 'def'"));
    }

    @Test
    void chainsThroughPrimaries() {
        assertEquals("def", eval("missing1 ?: missing2 ?: 'def'"));
    }

    @Test
    void fallbackPrimaryHasAValue() {
        assertEquals(1, eval("1 ?: missing"));
    }
}
