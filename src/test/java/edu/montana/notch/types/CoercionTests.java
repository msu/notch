package edu.montana.notch.types;

import edu.montana.notch.types.coercions.PrimitiveCoercions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CoercionTests {
    @Test
    public void primitivesAreAssignableToNumber() {
        assertFalse(Number.class.isAssignableFrom(Integer.TYPE));
        assertFalse(Number.class.isAssignableFrom(int.class));
        assertEquals(Integer.TYPE, int.class);
    }

    @Test
    public void booleanCoercesToBoxed() {
        NotchTypeCoercer c = PrimitiveCoercions.INSTANCE;
        final var p = NotchTypeSystem.getType(boolean.class);
        final var b = NotchTypeSystem.getType(Boolean.class);
        assertTrue(c.getCoercionCost(p, b) > 0, "could not coerce boolean -> Boolean");
        assertTrue(c.getCoercionCost(p, b) > 0, "could not coerce Boolean -> boolean");
    }
}
