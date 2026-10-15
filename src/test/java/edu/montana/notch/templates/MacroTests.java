package edu.montana.notch.templates;

import edu.montana.notch.ErrorTest;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MacroTests extends NotchTemplateTestBase {
    @Test
    public void shortParameterTypeName() {
        var result = renderString("""
                #macro greet(name: String)
                Hello, ${name}
                #end
                #expand greet("Ada")
                """);
        assertEquals("Hello, Ada\n", result);
    }

    @ErrorTest
    public void unknownParameterTypeFails() {
        registerTemplate("m", """
                #macro greet(name: com.example.Missing)
                Hello, ${name}
                #end
                #expand greet("Ada")
                """);
        var ex = assertThrows(RuntimeException.class, () -> renderTemplateForError("m"));
        var msg = messageOf(ex);
        assertContains("unknown type", msg, msg);
        assertContains("com.example.Missing", msg, msg);
    }
}
