package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.AssertContains.assertContains;
import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;
import static org.junit.jupiter.api.Assertions.*;

public class NotchPropertyTests {
    @Test
    public void undefinedSymbolIsUndefined() {
        RuntimeException exc = assertThrows(RuntimeException.class, () -> eval("foo"));
        assertContains("unknown variable \"foo\"", exc.getMessage());

        var result = eval("foo?");
        assertEquals(UNDEFINED, result);
    }

    @Test
    public void nullSymbolIsJustNull() {
        var value = eval("foo", "foo", null);
        assertNull(value);
        value = eval("foo?", "foo", null);
        assertNull(value);
    }

    @Test
    public void nullCoalesceIsUndefined() {
        var value = eval("foo.bar", "foo", null);
        assertEquals(UNDEFINED, value);
        value = eval("foo?.bar");
        assertEquals(UNDEFINED, value);
    }

    @Test
    public void invokingNullIsUndefined() {
        final var value = eval("foo.bar()", "foo", null);
        assertEquals(UNDEFINED, value);
    }

    @Test
    public void noSuchMethodIsUndefined() {
        final var value = eval("foo.bar()", "foo", new Foo());
        assertEquals(UNDEFINED, value);
    }

    class Foo {
        public void baz() {
        }
    }
}
