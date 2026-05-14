package edu.montana.notch.expressions;

import edu.montana.notch.runtime.NotchRuntime;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static org.junit.jupiter.api.Assertions.*;

public class NotchPropertyTests {
    public static void assertContains(String expectedContents, String string) {
        if (!string.contains(expectedContents)) {
            fail("The string '" + string + "' does not contain the expected contents: '" + expectedContents + "'");
        }
    }

    @Test
    public void undefinedSymbolEvaluatesToUndefined() {
        // Bare identifiers that aren't in scope evaluate to UNDEFINED; errors
        // only surface when something tries to *use* that value as if it were
        // defined (arithmetic, method invocation, etc.).
        var value = eval("foo");
        assertEquals(NotchRuntime.UNDEFINED, value);
    }

    @Test
    public void undefinedSymbolThrowsWhenUsed() {
        var ex = assertThrows(RuntimeException.class, () -> eval("foo + 1"));
        System.out.println(ex.getMessage());
    }

    @Test
    public void nullSymbolIsJustNull() {
        var value = eval("foo", "foo", null);
        assertNull(value);
    }

    @Test
    public void nullCoalesceIsJustNull() {
        var value = eval("foo.bar", "foo", null);
        assertNull(value);
    }

    @Test
    public void invokingNullThrows() {
        var ex = assertThrows(RuntimeException.class, () -> eval("foo.bar()", "foo", null));
        assertContains("unable to call \"bar\", \"foo\" was null", ex.getMessage());
    }

    @Test
    public void noSuchMethodThrows() {
        // TODO: "did you \"baz\"?"
        var ex = assertThrows(RuntimeException.class, () -> eval("foo.bar()", "foo", new Foo()));
        assertContains(
            "no such property/method named \"bar\" on \"foo\" (bigsky.notch.expressions.NotchPropertyTests$Foo)",
            ex.getMessage()
        );
    }

    class Foo {
        public void baz() {}
    }
}
