package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchFallbackTest {

    @Test
    void fallsBackOnNullLiteral() {
        assertEquals("def", eval("null ?: 'def'"));
    }

    @Test
    void fallsBackOnNullValuedVariable() {
        assertEquals("def", eval("a ?: 'def'", "a", null));
    }

    @Test
    void fallsBackOnUndefinedVariable() {
        assertEquals("def", eval("missing ?: 'def'"));
    }

    @Test
    void keepsNonNullPrimary() {
        assertEquals(1, eval("1 ?: 2"));
        assertEquals(false, eval("false ?: 'def'"));
        assertEquals(0, eval("0 ?: 'def'"));
    }
}
