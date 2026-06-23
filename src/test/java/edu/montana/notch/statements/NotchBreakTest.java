package edu.montana.notch.statements;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotchBreakTest {

    @Test
    void testBreakInForLoop() {
        String result = exec("""
                for x in [1, 2, 3, 4]
                    if x == 3
                        break
                    end
                    print(x)
                end
                """);
        assertEquals("1\n2\n", result);
    }

    @Test
    void testBreakInRepeatTimes() {
        String result = exec("""
                repeat 5 times
                    if it == 3
                        break
                    end
                    print(it)
                end
                """);
        assertEquals("1\n2\n", result);
    }

    @Test
    void testBreakInRepeatWhile() {
        String result = exec("""
                x = 0
                repeat while x < 10
                    x = x + 1
                    if x == 3
                        break
                    end
                    print(x)
                end
                """);
        assertEquals("1\n2\n", result);
    }

    @Test
    void testBreakInRepeatUntil() {
        String result = exec("""
                x = 0
                repeat until x >= 10
                    x = x + 1
                    if x == 3
                        break
                    end
                    print(x)
                end
                """);
        assertEquals("1\n2\n", result);
    }

    @Test
    void testNestedForLoopsBreakInnermostOnly() {
        String result = exec("""
                for x in [1, 2, 3]
                    for y in [1, 2, 3]
                        if y == 2
                            break
                        end
                        print(y)
                    end
                end
                """);
        assertEquals("1\n1\n1\n", result);
    }

    @Test
    void testNestedRepeatBreakInnermostOnly() {
        String result = exec("""
                repeat 3 times
                    outer = it
                    repeat 5 times
                        if it == 3
                            break
                        end
                        print(it)
                    end
                    print('--')
                end
                """);
        assertEquals("1\n2\n--\n1\n2\n--\n1\n2\n--\n", result);
    }

    @Test
    void testBreakOutsideLoopIsParseError() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                break
                print('unreachable')
                """));
        String msg = ex.getMessage();
        assertEquals(true, msg.contains("'break' outside a loop"),
                "expected 'break' outside a loop diagnostic, got: " + msg);
    }

    @Test
    void testBreakInIfOutsideLoopIsParseError() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                if true
                    break
                end
                """));
        String msg = ex.getMessage();
        assertEquals(true, msg.contains("'break' outside a loop"),
                "expected 'break' outside a loop diagnostic, got: " + msg);
    }
}
