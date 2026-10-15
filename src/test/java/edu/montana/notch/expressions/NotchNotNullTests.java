package edu.montana.notch.expressions;

import edu.montana.notch.ErrorTest;

import static edu.montana.notch.AssertContains.assertContains;
import static edu.montana.notch.NotchTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class NotchNotNullTests {
    @ErrorTest
    public void nullLiteralThrows() {
        var exc = assertThrows(RuntimeException.class, () -> eval("null!"));
        assertContains("this expression was null", exc.getMessage());
    }

    @ErrorTest
    public void undefinedThrows() {
        var exc = evalEx("foo?!");
        assertContains("this expression was <undefined>", exc.getMessage());
    }

    @ErrorTest
    public void nullPropertyThrows() {
        var exc = evalEx("foo.bar!", "foo", new Foo());
        assertContains("this expression was null", exc.getMessage());
    }

    @ErrorTest
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
