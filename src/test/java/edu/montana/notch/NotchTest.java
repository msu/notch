package edu.montana.notch;

import edu.montana.notch.chisel.ParseErrors;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotchTest {

    // --- single-arg eval (returns raw value, declared T inferred from call site) ---

    @Test
    public void evalIntegerLiteral() {
        int result = Notch.eval("42");
        assertEquals(42, result);
    }

    @Test
    public void evalArithmetic() {
        Object result = Notch.eval("1 + 2 * 3");
        assertEquals(7, result);
    }

    @Test
    public void evalBooleanLiteral() {
        Object result = Notch.eval("true");
        assertEquals(true, result);
    }

    @Test
    public void evalBooleanExpression() {
        Object result = Notch.eval("1 < 2");
        assertEquals(true, result);
    }

    @Test
    public void evalStringLiteral() {
        Object result = Notch.eval("'hello'");
        assertEquals("hello", result);
    }

    @Test
    public void evalParenthesizedExpression() {
        Object result = Notch.eval("(1 + 2) * 4");
        assertEquals(12, result);
    }

    @Test
    public void evalListLiteral() {
        Object result = Notch.eval("[1, 2, 3]");
        assertEquals(List.of(1, 2, 3), result);
    }

    // --- two-arg eval (with target type) ---

    @Test
    public void evalToMatchingType() {
        Integer result = Notch.eval("1 + 1", Integer.class);
        assertEquals(Integer.valueOf(2), result);
    }

    @Test
    public void evalToObjectAcceptsAnything() {
        Object result = Notch.eval("'foo'", Object.class);
        assertEquals("foo", result);
    }

    @Test
    public void evalToPrimitiveIntCoercesFromInteger() {
        // BoxedToPrimitive coercion: Integer -> int
        int result = Notch.eval("3 + 4", Integer.TYPE);
        assertEquals(7, result);
    }

    @Test
    public void evalToPrimitiveBooleanCoercesFromBoolean() {
        boolean result = Notch.eval("true", Boolean.TYPE);
        assertTrue(result);
    }

    @Test
    public void evalToStringWhenAlreadyString() {
        String result = Notch.eval("\"hi\"", String.class);
        assertEquals("hi", result);
    }

    @Test
    public void evalToIntegerFromBigDecimal() {
        // Skip the parser: feed a BigDecimal-producing expression. If the
        // language doesn't produce BigDecimal directly from a literal, this
        // exercises the BigDecimalToInt coercion path via the boxing API.
        // Falls back to verifying the coercion exists when invoked through
        // the public surface.
        BigDecimal bd = new BigDecimal("5");
        Object coerced = edu.montana.notch.types.coercions.Coercion
                .resolve(BigDecimal.class, Integer.class)
                .coerce(bd);
        assertEquals(5, coerced);
    }

    @Test
    public void evalThrowsWhenCoercionUnavailable() {
        // Integer result cannot be coerced to e.g. java.util.Date
        assertThrows(IllegalArgumentException.class,
                () -> Notch.eval("1", java.util.Date.class));
    }

    @Test
    public void evalReturnsInstanceWhenAssignableSupertype() {
        Number result = Notch.eval("1", Number.class);
        assertInstanceOf(Integer.class, result);
        assertEquals(1, result.intValue());
    }

    /*Ensure run entry point works
     See notch.cs.montana.edu/getting-started/maven/#run-notch-from-java
     */
    @Test
    public void runExecutesMultiStatementProgram() {
        assertEquals("2\n", NotchTestUtils.exec("""
                x = 1
                print(x + 1)
                """));
    }

    @Test
    public void runAcceptsStringProgramDirectly() {
        assertDoesNotThrow(() -> Notch.run("""
                x = 1
                print(x + 1)
                """));
    }

    @Test
    public void runAcceptsSingleExpressionProgram() {
        assertDoesNotThrow(() -> Notch.run("2 + 3"));
    }

    @Test
    public void runThrowsOnParseError() {
        assertThrows(ParseErrors.class, () -> Notch.run("if"));
    }
}
