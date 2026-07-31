package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchClosureTest {

    @Test
    void expressionBodyReturnsValue() {
        assertEquals(3, eval("(\\ s -> s.length)('abc')"));
        assertEquals(6, eval("(\\ n -> n + 1)(5)"));
    }

    @Test
    void blockBodyReturnsViaReturn() {
        assertEquals(6, eval("(\\ n -> { return n + 1 })(5)"));
    }

    @Test
    void blockBodyWithoutReturnYieldsUndefined() {
        // A block-body closure runs for side effects and yields undefined when no
        // `return` is hit (Gross's original semantics; value comes only via return).
        assertEquals(NotchRuntime.UNDEFINED, eval("(\\ n -> { x = n + 1 })(5)"));
    }

    @Test
    void returnInsideClosureShortCircuits() {
        assertEquals(1, eval("(\\ n -> { return 1  return 2 })(0)"));
    }
}
