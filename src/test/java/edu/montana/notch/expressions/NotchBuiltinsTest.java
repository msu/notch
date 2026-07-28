package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;

public class NotchBuiltinsTest {
    @Test
    public void testBuiltins() {
        var result = eval("structure('Hello, World!')");
        System.out.println(result);
    }
}
