package edu.montana.notch.types.coercions;

import edu.montana.notch.types.TypeRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class CoercionGraphTest {

    private static CoercionGraph defaults() {
        var graph = new CoercionGraph();
        graph.addAll(Coercion.defaults());
        return graph;
    }

    private static CoercionPath find(Class<?> from, Class<?> to) {
        return defaults().find(TypeRegistry.getType(from), to);
    }

    @Test
    public void sameTypeIsFreeAndIdentity() {
        var path = find(String.class, String.class);
        assertNotNull(path);
        assertEquals(0, path.cost());
        assertTrue(path.isIdentity());
        assertEquals("hi", path.apply("hi"));
    }

    @Test
    public void unboxingIsFree() {
        var path = find(Integer.class, Integer.TYPE);
        assertNotNull(path);
        assertEquals(0, path.cost());
        assertEquals(7, path.apply(7));
    }

    @Test
    public void widensToSuperclassAndInterface() {
        assertEquals(CoercionGraph.SUPERCLASS_COST, find(Integer.class, Number.class).cost());
        assertEquals(CoercionGraph.INTERFACE_COST, find(Integer.class, Comparable.class).cost());
    }

    @Test
    public void widensSourceToReachANumberEdge() {
        // Integer -> (widen) Number -> Long
        var path = find(Integer.class, Long.class);
        assertNotNull(path);
        assertEquals(1, path.steps().size());
        assertEquals(5L, path.apply(5));
    }

    @Test
    public void chainsCoercionIntoUnboxing() {
        // BigDecimal -> (widen) Number -> Integer -> int
        var path = find(BigDecimal.class, Integer.TYPE);
        assertNotNull(path);
        assertEquals(Integer.valueOf(3), path.apply(new BigDecimal("3.7")));
    }

    @Test
    public void widensArrayCovariantlyToReachIterable() {
        var path = find(String[].class, Iterable.class);
        assertNotNull(path);
        assertEquals(List.of("a", "b"), path.apply(new String[]{"a", "b"}));
    }

    @Test
    public void doesNotWidenAfterACoercion() {
        var graph = new CoercionGraph();
        // Sub -> Named is only legal if Named's supertypes are consulted after
        // the hop off Sub, which the graph must refuse to do.
        graph.add(new Coercion(Sub.class, Named.class, Function.identity()));
        assertNull(graph.find(TypeRegistry.getType(Sub.class), Runnable.class));
        assertNotNull(graph.find(TypeRegistry.getType(Sub.class), Named.class));
    }

    @Test
    public void unreachableTargetIsNull() {
        assertNull(find(String.class, Integer.class));
    }

    @Test
    public void prefersTheCheaperOfTwoChains() {
        // Integer -> int costs 0; Integer -> Number -> Long -> long costs more
        assertTrue(find(Integer.class, Integer.TYPE).cost() < find(Integer.class, Long.TYPE).cost());
    }

    public interface Named extends Runnable {
    }

    public static class Sub {
    }
}