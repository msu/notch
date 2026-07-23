package edu.montana.notch;

import edu.montana.notch.runtime.NotchClosure;
import edu.montana.notch.templates.runtime.RenderException;
import edu.montana.notch.types.NotchMethod;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.BetterMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import static edu.montana.notch.AssertContains.assertContains;
import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.*;

public class BootstrapTest {

    @Test
    public void bootstrap() {
        Object result = NotchTestUtils.eval("1 + 1");
        assertEquals(2, result);
    }

    @Test
    public void bootstrapPrintStatement() {
        String result = exec("print('foo')");
        assertEquals("foo\n", result);
    }

    @Test
    public void bootstrapPrintStatements() {
        String result = exec("print('foo') print('bar')");
        assertEquals("foo\nbar\n", result);
    }

    @Test
    public void bootstrapIfStatements() {
        String result = exec("if true print('foo') else print('bar') end");
        assertEquals("foo\n", result);

        result = exec("if false print('foo') else print('bar') end");
        assertEquals("bar\n", result);

        result = exec("if true print('foo') end");
        assertEquals("foo\n", result);

        result = exec("if false print('foo') end");
        assertEquals("", result);

    }

    @Test
    public void bootstrapForStatements() {
        String result = exec("for x in 'foo' print(x) end");
        assertEquals("f\no\no\n", result);

        result = exec("for x in 'foo' index i print(i) end");
        assertEquals("0\n1\n2\n", result);

        result = exec("for x in 'foo' index i print(i) end print(i?)");
        assertEquals("0\n1\n2\n<undefined>\n", result);

        var exc = assertThrows(RuntimeException.class, () -> exec("for x in 'foo' index i print(i) print(x) end print(x)"));
        assertContains("unknown variable \"x\"", exc.getMessage());
    }

    @Test
    public void bootstrapPropertyAccess() {
        ArrayList<Integer> list = new ArrayList<>(List.of(1, 2, 3));
        Object result = eval("x.size", "x", list);
        assertEquals(3, result);
    }

    @Test
    public void bootstrapMethodInvocation() {
        ArrayList<Integer> list = new ArrayList<>(List.of(1, 2, 3));
        Object result = eval("x.size()", "x", list);
        assertEquals(3, result);
    }

    @Test
    public void bootstrapTopLevelMethodInvocation() {
        NotchType testType = TypeSystem.getType(BootstrapTest.class);
        NotchMethod method = testType.getStaticMethod("foo");
        Object result = eval("foo(10)", "foo", method);
        assertEquals(10, result);
    }

    public static int foo(int i) {
        return i;
    }

    @Test
    public void bootstrapListLiteral() {
        Object result = eval("[1, 2, 3]");
        assertEquals(List.of(1, 2, 3), result);
        assertTrue(result instanceof BetterList<?>);
    }

    @Test
    public void bootstrapListLiteralCanHaveTrailingComma() {
        Object result = eval("[1, 2, 3,]");
        assertEquals(List.of(1, 2, 3), result);
        assertTrue(result instanceof BetterList<?>);
    }

    @Test
    public void bootstrapClosure() {
        NotchClosure result = (NotchClosure) eval("\\-> 1");
        assertEquals(1, result.call());
    }

    @Test
    public void bootstrapClosureAsArgument() {
        String result = exec("print( ['a', 'ab', 'abc'].map(\\ s -> s.length).toList() )");
        assertEquals("[1, 2, 3]\n", result);
    }

    @Test
    public void bootstrapClosureAsBiFunction() {
        BiFunction result = (BiFunction) eval("(\\ x, y -> x == y).toBiFunction()");
        assertEquals(true, result.apply(1, 1));
        assertEquals(false, result.apply(1, 2));
    }

    @Test
    public void bootstrapClosureAsBiPredicate() {
        BiPredicate result = (BiPredicate) eval("(\\ x, y -> x == y).toBiPredicate()");
        assertTrue(result.test(1, 1));
        assertFalse(result.test(1, 2));
    }

    @Test
    public void bootstrapMapLiteral() {
        Map result = (Map) eval("{foo=1, bar=2}");
        assertEquals(Map.of("foo", 1, "bar", 2), result);
        assertTrue(result instanceof BetterMap<?, ?>);
    }

    @Test
    public void bootstrapMapLiteralWithStringKeys() {
        Map result = (Map) eval("{'foo foo'=1, bar=2}");
        assertEquals(Map.of("foo foo", 1, "bar", 2), result);
        assertTrue(result instanceof BetterMap<?, ?>);
    }

    @Test
    public void bootstrapMapLiteralWithTerseStringKeys() {
        Map result = (Map) eval("{:foo123 = 1, bar=2}");
        assertEquals(Map.of("foo123", 1, "bar", 2), result);
        assertTrue(result instanceof BetterMap<?, ?>);
    }

    @Test
    public void bootstrapIndexingTest() {
        // lists
        String result = exec("""
                x = [1, 2, 3]
                print(x[1])
                """);
        assertEquals("2\n", result);

        // maps
        result = exec("""
                x = {foo=1, bar=2}
                print(x[:foo])
                """);
        assertEquals("1\n", result);

        // strings
        result = exec("""
                x = "asdf"
                print(x[1])
                """);
        assertEquals("s\n", result);

        // reflection
        result = exec("""
                print(x[:bar])
                """, "x", new Foo());
        assertEquals("bar\n", result);
    }

    public static class Foo {
        public String getBar() {
            return "bar";
        }
    }

    @Test
    public void bootstrapStupidMapTricks() {
        String result = exec("""
                x = { foo = \\-> "bar" }
                print(x.foo())
                """);
        assertEquals("bar\n", result);

        result = exec("""
                x = { foo = \\-> "bar" }
                print(x[:foo]())
                """);
        assertEquals("bar\n", result);
    }

    @Test
    public void bootstrapClassResolution() {
        Object eval = eval("java.lang.String");
        assertInstanceOf(NotchType.class, eval);
        NotchType notchType = (NotchType) eval;
        assertEquals("java.lang.String", notchType.getDisplayName());
    }

    @Test
    public void bootstrapPrimitiveResolution() {
        Object eval = eval("int");
        assertInstanceOf(NotchType.class, eval);
        NotchType notchType = (NotchType) eval;
        assertEquals("int", notchType.getDisplayName());
    }

    @Test
    public void bootstrapStaticMethodInvocation() {
        Object val = eval("java.util.List.of(1, 2, 3)");
        assertEquals(List.of(1, 2, 3), val);
    }

    @Test
    public void bootstrapStaticPropertyResolution() {
        Object val = eval("java.lang.Character.TYPE");
        assertEquals(Character.TYPE, val);
    }

    @Test
    public void bootstrapTypePropertyResolution() {
        assertEquals("java.lang.Object", eval("java.lang.Object.displayName"));
    }

    @Test
    public void bootstrapTypeAltPropertyResolution() {
        assertEquals("java.lang.Object", eval("java.lang.Object.display_name"));
    }

}
