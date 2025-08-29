package bigsky.notch;

import bigsky.notch.runtime.NotchClosure;
import bigsky.notch.types.NotchMethod;
import bigsky.notch.types.NotchType;
import bigsky.notch.types.TypeSystem;
import bigsky.utils.BetterList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static bigsky.notch.NotchTestUtils.*;
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

        result = exec("for x in 'foo' index i print(i) print(x) end print(x)");
        assertEquals("0\nf\n1\no\n2\no\n<undefined>\n", result);
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
        NotchMethod method = testType.getMethod("foo");
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
    public void bootstrapClosure() {
        NotchClosure result = (NotchClosure) eval("\\-> 1");
        assertEquals(1, result.call());
    }

    @Test
    public void bootstrapClosureAsArgument() {
        String result = exec("print( ['a', 'ab', 'abc'].map(\\ s -> s.length) )");
        assertEquals("[1, 2, 3]\n", result);
    }

}
