package bigsky.notch.templates;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MacroTests extends NotchTemplateTestBase {
    @Test
    public void basicMacroDeclareAndExpand() {
        var result = renderString("""
                #macro greeting
                <p>Hello!</p>
                #end
                #expand greeting()
                """);

        assertEquals("""
                <p>Hello!</p>
                """, result);
    }

    @Test
    public void macroWithParameters() {
        var result = renderString("""
                #macro greet(name)
                <p>Hello, ${name}!</p>
                #end
                #expand greet("Dillon")
                #expand greet("Carson")
                """);

        assertEquals("""
                <p>Hello, Dillon!</p>
                <p>Hello, Carson!</p>
                """, result);
    }

    @Test
    public void macroWithMultipleParameters() {
        var result = renderString("""
                #macro row(label, value)
                <tr><th>${label}</th><td>${value}</td></tr>
                #end
                #expand row("Name", "Carson")
                #expand row("Role", "Instructor")
                """);

        assertEquals("""
                <tr><th>Name</th><td>Carson</td></tr>
                <tr><th>Role</th><td>Instructor</td></tr>
                """, result);
    }

    @Test
    public void macroMissingArgumentBecomesUndefined() {
        var result = renderString("""
                #macro greet(name)
                <p>Hello, ${name ?: "stranger"}!</p>
                #end
                #expand greet()
                """);

        assertEquals("""
                <p>Hello, stranger!</p>
                """, result);
    }

    @Test
    public void macroArgumentsAreAccessibleAsList() {
        var result = renderString("""
                #macro sum
                ${arguments[0] + arguments[1]}
                #end
                #expand sum(2, 3)
                """);

        assertEquals("""
                5
                """, result);
    }

    @Test
    public void macroWithTypedParameterAccepts() {
        var result = renderString("""
                #macro shout(msg: java.lang.String)
                <b>${msg}</b>
                #end
                #expand shout("hello")
                """);

        assertEquals("""
                <b>hello</b>
                """, result);
    }

    @Test
    public void macroWithTypedParameterRejects() {
        registerTemplate("main", """
                #macro shout(msg: java.lang.String)
                <b>${msg}</b>
                #end
                #expand shout(42)
                """);

        assertThrows(RuntimeException.class, () -> renderTemplateForError("main"));
    }

    @Test
    public void macroSeesOuterScopeVariable() {
        var result = renderString("""
                #macro stamp
                (by ${author})
                #end
                #expand stamp()
                """, "author", "Carson");

        assertEquals("""
                (by Carson)
                """, result);
    }

    @Test
    public void macroHtmlEscapesExpressionOutput() {
        var result = renderString("""
                #macro show(v)
                ${v}
                #end
                #expand show("<script>")
                """);

        assertEquals("""
                &lt;script&gt;
                """, result);
    }

    @Test
    public void macroRedeclarationOverwrites() {
        var result = renderString("""
                #macro g
                first
                #end
                #expand g()
                #macro g
                second
                #end
                #expand g()
                """);

        assertEquals("""
                first
                second
                """, result);
    }
}
