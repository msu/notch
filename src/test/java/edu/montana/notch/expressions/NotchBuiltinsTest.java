package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotchBuiltinsTest {
    @Test
    public void testBuiltins() {
        var result = (int) eval(Integer.class, "random(1, 3)");
        assertTrue(result >= 1 && result <= 3);
    }
}
