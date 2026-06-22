package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchIsEqualityTest {

    @Test
    void integerIsLiteral() {
        assertEquals(true, eval("5 is 5"));
        assertEquals(false, eval("5 is 6"));
    }

    @Test
    void integerIsNotLiteral() {
        assertEquals(true, eval("5 is not 6"));
        assertEquals(false, eval("5 is not 5"));
    }

    @Test
    void stringIsString() {
        assertEquals(true, eval("'hello' is 'hello'"));
        assertEquals(false, eval("'hello' is 'world'"));
    }

    @Test
    void stringIsNotString() {
        assertEquals(true, eval("'hello' is not 'world'"));
        assertEquals(false, eval("'hello' is not 'hello'"));
    }

    @Test
    void boolIsBool() {
        assertEquals(true, eval("true is true"));
        assertEquals(false, eval("true is false"));
        assertEquals(true, eval("false is not true"));
    }

    @Test
    void crossTypeStrictness() {
        // NotchEquality uses Objects.equals
        assertEquals(false, eval("0 is '0'"));
        assertEquals(true, eval("0 is not '0'"));
    }

    @Test
    void isWithArithmeticRhs() {
        assertEquals(true, eval("8 is 5 + 3"));
        assertEquals(true, eval("2 is not 5 + 3"));
    }

    @Test
    void isBindsAtEqualityPrecedence() {
        assertEquals(true, eval("1 + 1 is 2"));
        assertEquals(true, eval("1 + 1 is not 3"));
        assertEquals(true, eval("2 * 3 is 6"));
        assertEquals(true, eval("10 - 4 is 2 + 4"));
        assertEquals(eval("1 + 1 == 2"), eval("1 + 1 is 2"));
    }

    @Test
    void isWithVariable() {
        String out = exec("""
                x = 7
                if x is 7
                  print('match')
                end
                """);
        assertEquals("match\n", out);
    }

    @Test
    void isNotWithVariable() {
        String out = exec("""
                x = 7
                if x is not 0
                  print('non-zero')
                end
                """);
        assertEquals("non-zero\n", out);
    }

    @Test
    void isStillRoutesToEmptyForBareword() {
        String out = exec("""
                items = [1, 2, 3]
                if items is not empty
                  print('has stuff')
                end
                """);
        assertEquals("has stuff\n", out);
    }
}
