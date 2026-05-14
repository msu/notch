package bigsky.notch.templates;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequireTests extends NotchTemplateTestBase {
    @Test
    public void singleRequirementHappyPath() {
        var result = renderString("""
                #require name: java.lang.String
                Hello, ${name}!
                """, "name", "Carson");
        assertEquals("Hello, Carson!\n", result);
    }

    @Test
    public void multipleRequirementsHappyPath() {
        var result = renderString("""
                #require name: java.lang.String, age: java.lang.Integer
                ${name} is ${age}
                """, "name", "Carson", "age", 40);
        assertEquals("Carson is 40\n", result);
    }

    @Test
    public void missingSymbolFails() {
        registerTemplate("m", """
                #require name: java.lang.String
                hi ${name}
                """);
        var ex = assertThrows(RuntimeException.class, () -> renderTemplateForError("m"));
        var msg = messageOf(ex);
        assertTrue(msg.contains("missing required symbol"), msg);
        assertTrue(msg.contains("\"name\""), msg);
    }

    @Test
    public void wrongTypeFails() {
        registerTemplate("m", """
                #require age: java.lang.Integer
                ${age}
                """);
        var ex = assertThrows(RuntimeException.class,
                () -> renderTemplateForError("m", "age", "not-an-int"));
        var msg = messageOf(ex);
        assertTrue(msg.contains("not assignable to java.lang.Integer"), msg);
    }

    @Test
    public void subtypeIsAccepted() {
        List<Integer> arr = new ArrayList<>(List.of(1, 2, 3));
        var result = renderString("""
                #require items: java.util.List
                #for n in items
                ${n}
                #end
                """, "items", arr);
        assertEquals("1\n2\n3\n", result);
    }

    @Test
    public void nullAcceptedWhenNoBang() {
        // Without `!`, null passes #require — the template just proceeds.
        var result = renderString("""
                #require maybe: java.lang.String
                ok
                """, "maybe", null);
        assertEquals("ok\n", result);
    }

    @Test
    public void nullRejectedWhenBang() {
        registerTemplate("m", """
                #require name: java.lang.String!
                hi ${name}
                """);
        var ex = assertThrows(RuntimeException.class,
                () -> renderTemplateForError("m", "name", null));
        var msg = messageOf(ex);
        assertTrue(msg.contains("must not be null"), msg);
        assertTrue(msg.contains("\"name\""), msg);
    }

    @Test
    public void bangedHappyPath() {
        var result = renderString("""
                #require name: java.lang.String!
                hi ${name}
                """, "name", "Carson");
        assertEquals("hi Carson\n", result);
    }

    @Test
    public void unknownTypeFails() {
        registerTemplate("m", """
                #require x: com.example.Missing
                ${x}
                """);
        var ex = assertThrows(RuntimeException.class,
                () -> renderTemplateForError("m", "x", "anything"));
        var msg = messageOf(ex);
        assertTrue(msg.contains("unknown type"), msg);
    }

    @Test
    public void bangedAndUnbangedMixed() {
        var result = renderString("""
                #require name: java.lang.String!, middle: java.lang.String
                ${name}${middle ?: ""}
                """, "name", "Carson", "middle", null);
        assertEquals("Carson\n", result);
    }

    private static String messageOf(Throwable ex) {
        var sb = new StringBuilder();
        Throwable t = ex;
        while (t != null) {
            sb.append(t.getClass().getName()).append(": ").append(t.getMessage()).append('\n');
            t = t.getCause();
        }
        return sb.toString();
    }
}
