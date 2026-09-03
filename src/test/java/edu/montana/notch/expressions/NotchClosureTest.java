package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchClosureTest {

    @Test
    void expressionBodyReturnsValueAndParametersBindPositionally() {
        assertEquals(2, eval("(\\ x, y -> x - y)(5, 3)"));
    }

    @Test
    void blockBodyReturnsViaReturn() {
        assertEquals(6, eval("(\\ n -> { return n + 1 })(5)"));
    }

    @Test
    void blockBodyWithoutReturnYieldsUndefined() {
        assertEquals(NotchRuntime.UNDEFINED, eval("(\\ n -> { x = n + 1 })(5)"));
    }

    @Test
    void returnInsideClosureShortCircuits() {
        assertEquals(1, eval("(\\ n -> { return 1  return 2 })(0)"));
    }

    @Test
    void blockBodyAssignmentMutatesAnEnclosingBinding() {
        String out = exec("""
                x = 1
                f = \\ n -> { x = n + 1 }
                f(5)
                print(x)
                """);
        assertEquals("6\n", out);
    }

    @Test
    void blockBodyAssignmentToANewNameDoesNotEscape() {
        String out = exec("""
                f = \\ n -> { x = n + 1 }
                f(5)
                print(x?)
                """);
        assertEquals("<undefined>\n", out);
    }
}
