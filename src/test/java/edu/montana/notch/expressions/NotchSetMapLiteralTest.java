package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotchSetMapLiteralTest {

    @Test
    void setLiteral() {
        Set<?> set = assertInstanceOf(Set.class, eval("{1, 2, 3}"));
        assertEquals(3, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
    }

    @Test
    void setLiteralSingleElement() {
        Set<?> set = assertInstanceOf(Set.class, eval("{42}"));
        assertEquals(1, set.size());
        assertTrue(set.contains(42));
    }

    @Test
    void emptySet() {
        Set<?> set = assertInstanceOf(Set.class, eval("{,}"));
        assertTrue(set.isEmpty());
    }

    @Test
    void arrowMap() {
        Map<?, ?> map = assertInstanceOf(Map.class, eval("{1 -> 'a', 2 -> 'b'}"));
        assertEquals(2, map.size());
        assertEquals("a", map.get(1));
        assertEquals("b", map.get(2));
    }

    @Test
    void arrowMapIndexAccess() {
        assertEquals("a", eval("{1 -> 'a', 2 -> 'b'}[1]"));
        assertEquals("a", eval("{1 -> 'a', 2 -> 'b'}.get(1)"));
    }

    @Test
    void emptyMap() {
        Map<?, ?> map = assertInstanceOf(Map.class, eval("{}"));
        assertTrue(map.isEmpty());
    }

    @Test
    void stringKeyedArrowMap() {
        Map<?, ?> map = assertInstanceOf(Map.class, eval("{'foo' -> 1, 'bar' -> 2}"));
        assertEquals(1, map.get("foo"));
        assertEquals(2, map.get("bar"));
    }

    @Test
    void undefinedBareKeyThrowsWithQuoteHint() {
        var ex = assertThrows(RuntimeException.class, () -> eval("{foo? -> 1}"));
        assertTrue(ex.getMessage().contains("undefined map key"),
                "expected an undefined-map-key error, got: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("quote it: 'foo'"),
                "expected a quote-it hint, got: " + ex.getMessage());
    }

    @Test
    void undefinedKeySuggestsNearbyVariable() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                food = 5
                x = {foo? -> 1}
                """));
        assertTrue(ex.getMessage().contains("did you mean the variable 'food'"),
                "expected a nearby-variable suggestion, got: " + ex.getMessage());
    }

    @Test
    void generalExpressionUndefinedKeyThrows() {
        var ex = assertThrows(RuntimeException.class, () -> eval("{p?.x -> 1}"));
        assertTrue(ex.getMessage().contains("undefined map key"),
                "expected an undefined-map-key error, got: " + ex.getMessage());
    }

    @Test
    void definedVariableKeyWorks() {
        assertEquals("v\n", exec("""
            k = 7
            print({k -> 'v'}[7])
            """));
    }
}
