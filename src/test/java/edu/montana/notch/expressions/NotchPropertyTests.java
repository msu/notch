package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    public void invokingNullThrowsError() {
        try {
            eval("foo.bar()", "foo", null);
            fail("expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("unable to call 'foo.bar'"));
        }
    }

    @Test
    public void noSuchMethodThrowsError() {
        try {
            eval("foo.bar()", new Foo());
            fail("expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("unable to call 'foo.bar'"));
        }
    }

    class Foo {
        public void baz() {
        }
    }
}
