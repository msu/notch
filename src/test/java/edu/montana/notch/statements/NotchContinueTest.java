package edu.montana.notch.statements;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotchContinueTest {

    @Test
    void testContinueInForLoop() {
        String result = exec("""
                for x in [1, 2, 3, 4]
                    if x == 2
                        continue
                    end
                    print(x)
                end
                """);
        assertEquals("1\n3\n4\n", result);
    }

    @Test
    void testContinueInRepeatTimes() {
        String result = exec("""
                repeat 5 times
                    if it == 3
                        continue
                    end
                    print(it)
                end
                """);
        assertEquals("1\n2\n4\n5\n", result);
    }

    @Test
    void testContinueInRepeatWhile() {
        String result = exec("""
                x = 0
                repeat while x < 5
                    x = x + 1
                    if x == 3
                        continue
                    end
                    print(x)
                end
                """);
        assertEquals("1\n2\n4\n5\n", result);
    }

    @Test
    void testContinueInRepeatUntil() {
        String result = exec("""
                x = 0
                repeat until x >= 5
                    x = x + 1
                    if x == 3
                        continue
                    end
                    print(x)
                end
                """);
        assertEquals("1\n2\n4\n5\n", result);
    }

    @Test
    void testNestedForLoopsContinueInnermostOnly() {
        String result = exec("""
                for x in [1, 2]
                    for y in [1, 2, 3]
                        if y == 2
                            continue
                        end
                        print(y)
                    end
                    print('--')
                end
                """);
        assertEquals("1\n3\n--\n1\n3\n--\n", result);
    }

    @Test
    void testContinueOutsideAnyLoopIsParseError() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                continue
                print('unreachable')
                """));
        String msg = ex.getMessage();
        assertEquals(true, msg.contains("'continue' outside a loop"),
                "expected 'continue' outside a loop diagnostic, got: " + msg);
    }

    @Test
    void testContinueInIfOutsideLoopIsParseError() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                if true
                    continue
                end
                """));
        String msg = ex.getMessage();
        assertEquals(true, msg.contains("'continue' outside a loop"),
                "expected 'continue' outside a loop diagnostic, got: " + msg);
    }
}
