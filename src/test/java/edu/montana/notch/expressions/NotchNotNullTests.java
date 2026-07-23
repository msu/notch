package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.AssertContains.assertContains;
import static edu.montana.notch.NotchTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class NotchNotNullTests {
    @Test
    public void nullLiteralThrows() {
        var exc = assertThrows(RuntimeException.class, () -> eval("null!"));
        assertContains("this expression was null", exc.getMessage());
    }

    @Test
    public void undefinedThrows() {
        var exc = evalEx("foo?!");
        assertContains("this expression was <undefined>", exc.getMessage());
    }

    @Test
    public void nullPropertyThrows() {
        var exc = evalEx("foo.bar!", "foo", new Foo());
        assertContains("this expression was null", exc.getMessage());
    }

    @Test
    public void nullMethodThrows() {
        var exc = evalEx("foo.baz()!", "foo", new Foo());
        assertContains("this expression was null", exc.getMessage());
    }

    public static class Foo {
        public Integer bar;

        public String baz() {
            return null;
        }
    }
}
