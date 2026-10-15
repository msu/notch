package edu.montana.notch.statements;

import edu.montana.notch.ErrorTest;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.AssertContains.assertContains;
import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotchReturnIfTest {

    @Test
    void returnsEarlyWhenConditionTrue() {
        String out = exec("""
                function f(n)
                  print('before')
                  return if n > 5
                  print('after')
                end
                f(10)
                """);
        assertEquals("before\n", out);
    }

    @Test
    void fallsThroughWhenConditionFalse() {
        String out = exec("""
                function f(n)
                  print('before')
                  return if n > 5
                  print('after')
                end
                f(1)
                """);
        assertEquals("before\nafter\n", out);
    }

    @Test
    void producesUndefinedValue() {
        String out = exec("""
                function f(n)
                  return if n > 5
                  return 'fell through'
                end
                print(f(10))
                """);
        assertEquals("<undefined>\n", out);
    }

    @Test
    void conditionCanUseBooleanLogic() {
        String out = exec("""
                function f(n)
                  return if n > 5 and n < 10
                  print('kept going')
                end
                f(7)
                f(20)
                """);
        assertEquals("kept going\n", out);
    }

    @Test
    void exitsFromInsideLoop() {
        String out = exec("""
                function f(limit)
                  for x in 'abcde'
                    return if x == limit
                    print(x)
                  end
                  print('loop done')
                end
                f('c')
                """);
        assertEquals("a\nb\n", out);
    }

    @Test
    void worksInsideMethod() {
        String out = exec("""
                class Counter(n)
                  function report()
                    return if this.n == 0
                    print('n is ' + this.n)
                  end
                end
                new Counter(0).report()
                new Counter(3).report()
                """);
        assertEquals("n is 3\n", out);
    }

    @Test
    void ifOnFollowingLineIsNotAModifier() {
        String out = exec("""
                function f(n)
                  return
                  if n > 5
                    print('unreachable')
                  end
                end
                f(10)
                print('done')
                """);
        assertEquals("done\n", out);
    }

    @ErrorTest
    void nonBooleanConditionThrows() {
        RuntimeException exc = assertThrows(RuntimeException.class, () -> exec("""
                function f(n)
                  return if n
                end
                f(3)
                """));
        assertContains("unable to cast java.lang.Integer to java.lang.Boolean", exc.getMessage());
    }

    @ErrorTest
    void outsideFunctionThrows() {
        RuntimeException exc = assertThrows(RuntimeException.class, () -> exec("""
                return if true
                """));
        assertContains("'return' outside a function", exc.getMessage());
    }

    @ErrorTest
    void missingConditionThrows() {
        // No condition on the 'return' line, so 'if' is parsed as a plain if statement.
        RuntimeException exc = assertThrows(RuntimeException.class, () -> exec("""
                function f()
                  return if
                end
                """));
        assertContains("expected a conditional expression after 'if'", exc.getMessage());
    }
}