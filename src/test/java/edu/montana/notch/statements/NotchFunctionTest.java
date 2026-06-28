package edu.montana.notch.statements;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotchFunctionTest {

    @Test
    void declareAndCallWithReturn() {
        String out = exec("""
                function add(a, b)
                  return a + b
                end
                print(add(2, 3))
                """);
        assertEquals("5\n", out);
    }

    @Test
    void recursion() {
        String out = exec("""
                function fact(n)
                  if n <= 1
                    return 1
                  end
                  return n * fact(n - 1)
                end
                print(fact(5))
                """);
        assertEquals("120\n", out);
    }

    @Test
    void closesOverEnclosingScope() {
        String out = exec("""
                x = 10
                function addX(n)
                  return n + x
                end
                print(addX(5))
                """);
        assertEquals("15\n", out);
    }

    @Test
    void bareReturnExitsEarly() {
        String out = exec("""
                function greet()
                  print('hi')
                  return
                  print('unreachable')
                end
                ignored = greet()
                """);
        assertEquals("hi\n", out);
    }

    @Test
    void typedSignatureParsesAndRunsUnenforced() {
        // Types parsed for syntax acceptance but not enforced.
        String out = exec("""
                function add(a: Int, b: Int): Int
                  return a + b
                end
                print(add(2, 3))
                """);
        assertEquals("5\n", out);
    }

    @Test
    void arityMismatchThrows() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                function add(a, b)
                  return a + b
                end
                print(add(1))
                """));
        assertTrue(ex.getMessage().contains("does not match the number of parameters"),
                "expected arity mismatch message, got: " + ex.getMessage());
    }

    @Test
    void returnOutsideFunctionIsParseError() {
        var ex = assertThrows(RuntimeException.class, () -> exec("return 5"));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected message about return outside a function, got: " + ex.getMessage());
    }

    @Test
    void bareCallExpressionStatementRuns() {
        assertEquals("ran\n", exec("""
                function noop()
                    print("ran")
                end
                noop()
                """));
    }

    @Test
    void callingUndefinedFunctionThrowsError() {
        var ex = assertThrows(RuntimeException.class, () -> exec("add()"));
        assertTrue(ex.getMessage().contains("undefined function 'add'"),
                "expected undefined function message, got: " + ex.getMessage());
    }

    @Test
    void callingUndefinedMethodThrowsError() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                x = 5
                x.add()
                """));
        assertTrue(ex.getMessage().contains("no method 'add' on Integer"),
                "expected no-method message, got: " + ex.getMessage());
    }

    @Test
    void nearMissMethodSuggestsCorrectName() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                s = "hello"
                s.lengthh()
                """));
        assertTrue(ex.getMessage().contains("Did you mean 'length'"),
                "expected Did-you-mean hint for near-miss method name, got: " + ex.getMessage());
    }
}
