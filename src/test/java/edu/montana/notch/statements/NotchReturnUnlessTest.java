package edu.montana.notch.statements;

import edu.montana.notch.ErrorTest;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.AssertContains.assertContains;
import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotchReturnUnlessTest {

    @Test
    void returnsEarlyWhenConditionFalse() {
        String out = exec("""
                function f(n)
                  print('before')
                  return unless n > 5
                  print('after')
                end
                f(1)
                """);
        assertEquals("before\n", out);
    }

    @Test
    void fallsThroughWhenConditionTrue() {
        String out = exec("""
                function f(n)
                  print('before')
                  return unless n > 5
                  print('after')
                end
                f(10)
                """);
        assertEquals("before\nafter\n", out);
    }

    @Test
    void producesUndefinedValue() {
        String out = exec("""
                function f(n)
                  return unless n > 5
                  return 'fell through'
                end
                print(f(1))
                """);
        assertEquals("<undefined>\n", out);
    }

    @Test
    void guardsWithNullCheck() {
        String out = exec("""
                function f(value)
                  return unless value != null
                  print('got ' + value)
                end
                f(null)
                f('x')
                """);
        assertEquals("got x\n", out);
    }

    @Test
    void exitsFromInsideLoop() {
        String out = exec("""
                function f(limit)
                  for x in 'abcde'
                    return unless x != limit
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
                    return unless this.n > 0
                    print('n is ' + this.n)
                  end
                end
                new Counter(0).report()
                new Counter(3).report()
                """);
        assertEquals("n is 3\n", out);
    }

    @ErrorTest
    void unlessOnFollowingLineIsNotAModifier() {
        RuntimeException exc = assertThrows(RuntimeException.class, () -> exec("""
                function f(n)
                  return
                  unless n > 5
                end
                f(10)
                """));
        assertContains("unless", exc.getMessage());
    }

    @ErrorTest
    void nonBooleanConditionThrows() {
        RuntimeException exc = assertThrows(RuntimeException.class, () -> exec("""
                function f(n)
                  return unless n
                end
                f(3)
                """));
        assertContains("unable to cast java.lang.Integer to java.lang.Boolean", exc.getMessage());
    }

    @ErrorTest
    void outsideFunctionThrows() {
        RuntimeException exc = assertThrows(RuntimeException.class, () -> exec("""
                return unless true
                """));
        assertContains("'return' outside a function", exc.getMessage());
    }

    @ErrorTest
    void missingConditionThrows() {
        // No condition on the 'return' line, so 'unless' is left dangling as its own statement.
        RuntimeException exc = assertThrows(RuntimeException.class, () -> exec("""
                function f()
                  return unless
                end
                """));
        assertContains("expected a statement", exc.getMessage());
    }
}