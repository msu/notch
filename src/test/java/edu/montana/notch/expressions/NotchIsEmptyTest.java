package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchIsEmptyTest {

    @Test
    void emptyString() {
        assertEquals(true, eval("'' is empty"));
        assertEquals(false, eval("'' is not empty"));
    }

    @Test
    void nonEmptyString() {
        assertEquals(false, eval("'abc' is empty"));
        assertEquals(true, eval("'abc' is not empty"));
    }

    @Test
    void nullIsEmpty() {
        assertEquals(true, eval("null is empty"));
        assertEquals(false, eval("null is not empty"));
    }

    @Test
    void lists() {
        assertEquals(true, eval("[] is empty"));
        assertEquals(false, eval("[1] is empty"));
        assertEquals(true, eval("[1] is not empty"));
    }
}
