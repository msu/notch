package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchNotPrefixTest {

    @Test
    void notTrue() {
        assertEquals(false, eval("not true"));
    }

    @Test
    void notFalse() {
        assertEquals(true, eval("not false"));
    }

    @Test
    void notZero() {
        assertEquals(true, eval("not 0"));
    }

    @Test
    void notNonZero() {
        assertEquals(true, eval("not 7"));
    }

    @Test
    void notNotTrue() {
        assertEquals(true, eval("not not true"));
    }

    @Test
    void notWithLogicalAnd() {
        assertEquals(true, eval("not false and true"));
    }

    @Test
    void notWithLogicalOr() {
        assertEquals(true, eval("not false or false"));
    }

    @Test
    void notWithComparison() {
        assertEquals(true, eval("not true == false"));
    }

    @Test
    void notInIf() {
        String out = exec("""
                x = false
                if not x
                  print('x is falsy')
                end
                """);
        assertEquals("x is falsy\n", out);
    }
}
