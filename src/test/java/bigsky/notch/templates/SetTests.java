package bigsky.notch.templates;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SetTests extends NotchTemplateTestBase {
    @Test
    public void setLiteralValue() {
        var result = renderString("""
                #set greeting = "hello"
                ${greeting}
                """);
        assertEquals("hello\n", result);
    }

    @Test
    public void setExpressionValue() {
        var result = renderString("""
                #set sum = 2 + 3
                ${sum}
                """);
        assertEquals("5\n", result);
    }

    @Test
    public void setFromContextVariable() {
        var result = renderString("""
                #set shout = name + "!"
                ${shout}
                """, "name", "Carson");
        assertEquals("Carson!\n", result);
    }

    @Test
    public void setInsideForIsLoopLocal() {
        var result = renderString("""
                #for n in [1, 2, 3]
                #set doubled = n * 2
                ${doubled}
                #end
                """);
        assertEquals("2\n4\n6\n", result);
    }

    @Test
    public void setOverwritesPreviousValue() {
        var result = renderString("""
                #set x = "first"
                ${x}
                #set x = "second"
                ${x}
                """);
        assertEquals("first\nsecond\n", result);
    }
}
