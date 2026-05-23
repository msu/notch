package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NotchPropertyTests {
    @Test
    public void undefinedSymbolIsUndefined() {
        final var value = eval("foo");
        assertEquals(UNDEFINED, value);
    }

    @Test
    public void nullSymbolIsJustNull() {
        var value = eval("foo", "foo", null);
        assertNull(value);
    }

    @Test
    public void nullCoalesceIsUndefined() {
        var value = eval("foo.bar", "foo", null);
        assertEquals(UNDEFINED, value);
    }

    @Test
    public void invokingNullIsUndefined() {
        final var value = eval("foo.bar()", "foo", null);
        assertEquals(UNDEFINED, value);
    }

    @Test
    public void noSuchMethodIsUndefined() {
        final var value = eval("foo.bar()", new Foo());
        assertEquals(UNDEFINED, value);
    }

    class Foo {
        public void baz() {
        }
    }
}
