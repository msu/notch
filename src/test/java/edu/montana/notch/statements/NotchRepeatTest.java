package edu.montana.notch.statements;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.AssertContains.assertContains;
import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotchRepeatTest {

    @Test
    void testRepeatTimesBindsIt() {
        String result = exec("""
                repeat 3 times
                    print(it)
                end
                """);
        assertEquals("1\n2\n3\n", result);
    }

    @Test
    void testRepeatZeroTimes() {
        String result = exec("""
                repeat 0 times
                    print('nope')
                end
                print('after')
                """);
        assertEquals("after\n", result);
    }

    @Test
    void testRepeatNegative() {
        String result = exec("""
                n = 0 - 3
                repeat n times
                    print('nope')
                end
                print('after')
                """);
        assertEquals("after\n", result);
    }

    @Test
    void testRepeatTimesWithVariable() {
        String result = exec("""
                n = 5
                repeat n times
                    print(it)
                end
                """);
        assertEquals("1\n2\n3\n4\n5\n", result);
    }

    @Test
    void testRepeatWhile() {
        String result = exec("""
                x = 0
                repeat while x < 3
                    x = x + 1
                    print(x)
                end
                """);
        assertEquals("1\n2\n3\n", result);
    }

    @Test
    void testRepeatUntil() {
        String result = exec("""
                x = 0
                repeat until x >= 3
                    x = x + 1
                    print(x)
                end
                """);
        assertEquals("1\n2\n3\n", result);
    }

    @Test
    void testRepeatItIsRebindable() {
        String result = exec("""
                repeat 5 times
                    if it == 3
                        it = 99
                    end
                    print(it)
                end
                """);
        assertEquals("1\n2\n99\n4\n5\n", result);
    }

    @Test
    void testRepeatItScopeIsolated() {
        var exc = assertThrows(RuntimeException.class, () -> exec("""
                repeat 2 times
                    print(it)
                end
                print(it)
                """));
        assertContains("unknown variable \"it\"", exc.getMessage());
    }

    @Test
    void innerRepeatItDoesNotClobberOuterIt() {
        String result = exec("""
                repeat 2 times
                    outer = it
                    repeat 3 times
                        print(it)
                    end
                    print(outer)
                end
                """);
        assertEquals("1\n2\n3\n1\n" +
                     "1\n2\n3\n2\n", result);
    }

    @Test
    void testRepeatWhileBreakExits() {
        String result = exec("""
                repeat while true
                    print('x')
                    break
                end
                """);
        assertEquals("x\n", result);
    }

    @Test
    void repeatTimesNonNumberTargetedError() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                repeat 'oops' times
                    print('nope')
                end
                """));
        var msg = ex.getMessage();
        assertTrue(msg.contains("'repeat N times' expects a number"),
                "expected targeted error, got: " + ex.getMessage());
    }

    @Test
    void whileChecksCondition() {
        String result = exec("""
                repeat while false
                    print('nope')
                end
                print('after')
                """);
        assertEquals("after\n", result);
    }

    @Test
    void untilChecksCondition() {
        String result = exec("""
                repeat until true
                    print('nope')
                end
                print('after')
                """);
        assertEquals("after\n", result);
    }
}
