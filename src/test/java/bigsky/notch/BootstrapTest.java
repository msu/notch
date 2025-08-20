package bigsky.notch;

import org.junit.jupiter.api.Test;

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

}
