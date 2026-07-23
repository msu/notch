package edu.montana.notch.templates;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RawTests extends NotchTemplateTestBase {

    @Test
    public void testBasic() {
        registerTemplate("main", """
Hi!
#raw
There!
#endraw
                """);

        var content = renderTemplate("main");
        assertEquals("Hi!\nThere!\n", content);
    }

    @Test
    public void testInterpolation() {
        registerTemplate("main", """
#raw
My name is ${:Dillon}
#endraw
                """);
        var content = renderTemplate("main");
        assertEquals("My name is ${:Dillon}\n", content);
    }

    @Test
    public void testSubcommands() {
        registerTemplate("main", """
                #raw
                #comment
                This should still appear
                #end
                #for x in [1, 2, 3]
                    foo = ${x}
                #end
                #endraw
                """);
        var content = renderTemplate("main");
        assertEquals("""
                #comment
                This should still appear
                #end
                #for x in [1, 2, 3]
                    foo = ${x}
                #end
                """, content);
    }
}
