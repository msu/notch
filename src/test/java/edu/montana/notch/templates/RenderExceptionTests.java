package edu.montana.notch.templates;

import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.templates.runtime.RenderException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

public class RenderExceptionTests extends NotchTemplateTestBase {
    private RenderException renderFailure(String uri) {
        return assertThrows(RenderException.class, () -> renderTemplateForError(uri));
    }

    @Test
    void messageIncludesTemplateStackTrace() {
        registerTemplate("main.html", "a\n#include \"leaf.html\"\n");
        registerTemplate("leaf.html", "${java.lang.Integer.parseInt(\"nope\")}\n");

        var message = renderFailure("main.html").getMessage();
        assertContains("Notch Stack Trace:", message);
        assertContains("at ${...}(leaf.html:1)", message);
        assertContains("at #include(main.html:2)", message);
        // the frames of whoever called into the template engine are trimmed off
        assertFalse(message.contains("NotchTemplateTestBase"), message);
        assertTrue(renderFailure("main.html").render(true).contains("NotchTemplateTestBase"));
    }

    @Test
    void runtimeErrorIsTheCause() {
        registerTemplate("main.html", "${java.lang.Integer.parseInt(\"nope\")}\n");

        var ex = renderFailure("main.html");
        var nre = assertInstanceOf(NotchRuntimeException.class, ex.getCause());
        assertSame(nre, ex.first(NotchRuntimeException.class).orElseThrow());
        assertEquals(List.of(nre.diagnostic), ex.getDiagnostics());
        assertSame(nre.stackTrace, ex.getNotchStackTrace().orElseThrow());
    }

    @Test
    void spanPointsAtTheFailure() {
        registerTemplate("main.html", "a\nb\n${java.lang.Integer.parseInt(\"nope\")}\n");

        var span = renderFailure("main.html").getSpan().orElseThrow();
        assertEquals("main.html", span.source().id);
        assertEquals(3, span.startLine());
    }

    @Test
    void parseErrorsHaveNoNotchStackTrace() {
        registerTemplate("main.html", "#for\n");

        var ex = renderFailure("main.html");
        assertTrue(ex.first(ParseException.class).isPresent());
        assertTrue(ex.getNotchStackTrace().isEmpty());
        assertFalse(ex.getDiagnostics().isEmpty());
    }

    @Test
    void nestedRenderExceptionsAreFlattened() {
        var inner = new RenderException(new IllegalStateException("boom"));
        var outer = new RenderException(List.of(inner, new IllegalArgumentException("bang")));

        assertEquals(2, outer.getChildren().size());
        assertInstanceOf(IllegalStateException.class, outer.getCause());
        assertEquals(1, outer.getSuppressed().length);
        assertSame(inner, RenderException.wrap(inner));
    }
}
