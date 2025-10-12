package bigsky.notch.expressions;

import org.junit.jupiter.api.Test;

import static bigsky.notch.NotchTestUtils.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotchBasicExpressionTest {

    @Test
    public void testIntegers() {
        assertEquals(1, eval("1"));
        assertEquals(42, eval("42"));
        assertEquals(0, eval("0"));
        assertEquals(-1, eval("-1"));
    }

    @Test
    public void testBooleans() {
        assertEquals(true, eval("true"));
        assertEquals(false, eval("false"));
    }

    @Test
    public void testStrings() {
        assertEquals("hello", eval("\"hello\""));
        assertEquals("world", eval("'world'"));
    }

    @Test
    public void testAddition() {
        assertEquals(2, eval("1 + 1"));
        assertEquals(10, eval("5 + 5"));
        assertEquals(15, eval("5 + 5 + 5"));
    }

    @Test
    public void testSubtraction() {
        assertEquals(0, eval("1 - 1"));
        assertEquals(5, eval("10 - 5"));
        assertEquals(-5, eval("5 - 10"));
    }

    @Test
    public void testMultiplication() {
        assertEquals(6, eval("2 * 3"));
        assertEquals(20, eval("4 * 5"));
        assertEquals(0, eval("5 * 0"));
    }

    @Test
    public void testDivision() {
        assertEquals(2, eval("6 / 3"));
        assertEquals(5, eval("10 / 2"));
        assertEquals(1, eval("5 / 5"));
    }

    @Test
    public void testRemainder() {
        assertEquals(1, eval("5 % 2"));
        assertEquals(0, eval("10 % 5"));
        assertEquals(3, eval("13 % 5"));
    }

    @Test
    public void testParenthesizedExpressions() {
        assertEquals(14, eval("2 * (3 + 4)"));
        assertEquals(10, eval("(2 + 3) * 2"));
        assertEquals(9, eval("(1 + 2) * (2 + 1)"));
    }

    @Test
    public void testOperatorPrecedence() {
        assertEquals(7, eval("1 + 2 * 3"));
        assertEquals(11, eval("5 + 12 / 2"));
        assertEquals(8, eval("2 * 3 + 2"));
        assertEquals(10, eval("20 - 2 * 5"));
    }

    @Test
    public void testComparison() {
        assertEquals(true, eval("5 > 3"));
        assertEquals(false, eval("3 > 5"));
        assertEquals(true, eval("3 < 5"));
        assertEquals(false, eval("5 < 3"));
        assertEquals(true, eval("5 >= 5"));
        assertEquals(true, eval("5 <= 5"));
        assertEquals(false, eval("5 > 5"));
    }

    @Test
    public void testEquality() {
        assertEquals(true, eval("5 == 5"));
        assertEquals(false, eval("5 == 3"));
        assertEquals(true, eval("5 != 3"));
        assertEquals(false, eval("5 != 5"));
    }

    @Test
    public void testLogicalOperators() {
        assertEquals(true, eval("true && true"));
        assertEquals(false, eval("true && false"));
        assertEquals(false, eval("false && true"));
        assertEquals(false, eval("false && false"));
        assertEquals(true, eval("true || false"));
        assertEquals(true, eval("false || true"));
        assertEquals(false, eval("false || false"));
    }

    @Test
    public void testLogicalOperatorsWords() {
        assertEquals(true, eval("true and true"));
        assertEquals(false, eval("true and false"));
        assertEquals(true, eval("true or false"));
        assertEquals(false, eval("false or false"));
    }

    @Test
    public void testNotOperator() {
        assertEquals(false, eval("not true"));
        assertEquals(true, eval("not false"));
        assertEquals(false, eval("!true"));
        assertEquals(true, eval("!false"));
    }

    @Test
    public void testComplexLogicalExpressions() {
        assertEquals(true, eval("5 > 3 && 10 < 20"));
        assertEquals(false, eval("5 > 3 && 10 > 20"));
        assertEquals(true, eval("5 > 10 || 10 < 20"));
        assertEquals(true, eval("!(5 > 10)"));
    }

}
