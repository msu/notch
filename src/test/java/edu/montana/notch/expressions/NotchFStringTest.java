package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.AssertContains.assertContains;
import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.NotchTestUtils.evalEx;
import static org.junit.jupiter.api.Assertions.*;

public class NotchFStringTest {
    @Test
    public void testUnescapedString() {
        assertEquals("Hello {name}", eval("\"Hello {name}\""));
        assertEquals("Hello {name}", eval("'Hello {name}'"));
        assertContains("extra tokens", evalEx(":{name}").getMessage());
    }

    @Test
    public void testLiteralEscapes() {
        assertEquals("123", eval("f'{123}'"));
        assertEquals("123", eval("f\"{123}\""));
        assertEquals("-123", eval("f'{123 * -1}'"));
        assertEquals("true or false?", eval("f'{true} or {false}?'"));
    }

    @Test
    public void testValues() {
        assertEquals("Hello, soup", eval("f'Hello, {name}'", "name", "soup"));
    }

    @Test
    public void testUnterminated() {
        // TODO: improve this error message to say unterminated '{' group
        var msg = evalEx("f'Hello, {goodbye!'").getMessage();
        assertContains("unterminated string", msg);
        assertContains("within f-string", msg);
    }

    @Test
    public void weirdTerseExpression() {
        assertEquals("<two>", eval("f:<{one ?: two ?: three}>", "two", "two"));
    }

    @Test
    public void undefinedAndNull() {
        assertEquals("<undefined> null", eval("f'{x?} {null}'"));
    }

    @Test
    public void javaArrays() {
        // TODO: we should probably have a formatter for these...
        var res = (String) eval("f:{x}", "x", new Object[]{1, 2.0, "3"});
        assertTrue(res.startsWith("[Ljava.lang.Object;"));
    }
}
