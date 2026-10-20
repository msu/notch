package edu.montana.notch;

import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.BasicNotchTemplateCommands;
import edu.montana.notch.templates.NotchTemplates;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

public class NotchRenderTest {

    void render(String template, Appendable w, Object... vars) {
        var templates = new NotchTemplates();
        BasicNotchTemplateCommands.addTo(templates);
        templates.renderTemplate(template, NotchTestUtils.map(vars), new Drain(w));
    }

    String render(String template, Object... vars) {
        var templates = new NotchTemplates();
        BasicNotchTemplateCommands.addTo(templates);
        return templates.renderTemplate(template, NotchTestUtils.map(vars));
    }

    @Test
    public void renderWithNoVars() {
        String result = render("notch_render/static.notch");
        assertEquals("static content", result);
    }

    @Test
    public void renderWithSingleVar() {
        String result = render("notch_render/hello.notch", "name", "Alice");
        assertEquals("Hello, Alice!", result);
    }

    @Test
    public void renderWithMultipleVars() {
        String result = render("notch_render/greet.notch", "greeting", "Howdy", "name", "Bob");
        assertEquals("Howdy, Bob!", result);
    }

    @Test
    public void renderEscapesHtmlByDefault() {
        String result = render("notch_render/hello.notch", "name", "<script>");
        assertEquals("Hello, &lt;script&gt;!", result);
    }

    @Test
    public void renderToStringBuilder() {
        var sb = new StringBuilder("[prefix]");
        render("notch_render/hello.notch", sb, "name", "Carol");
        assertEquals("[prefix]Hello, Carol!", sb.toString());
    }

    @Test
    public void renderToStringWriter() {
        var sw = new StringWriter();
        render("notch_render/greet.notch", sw, "greeting", "Hi", "name", "Dave");
        assertEquals("Hi, Dave!", sw.toString());
    }

    @Test
    public void renderToAppendableWithNoVars() {
        var sb = new StringBuilder();
        render("notch_render/static.notch", sb);
        assertEquals("static content", sb.toString());
    }

    @ErrorTest
    public void renderOddArityThrows() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> render("notch_render/hello.notch", "name"));
        assertTrue(ex.getMessage().contains("even number"), ex.getMessage());
    }

    @ErrorTest
    public void renderNonStringKeyThrows() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> render("notch_render/hello.notch", 42, "Alice"));
        assertTrue(ex.getMessage().contains("must be a String"), ex.getMessage());
    }

    @Test
    public void renderMissingTemplateThrows() {
        assertThrows(RuntimeException.class,
                () -> render("notch_render/does_not_exist.notch"));
    }

    @Test
    public void renderAppendableOddArityThrows() {
        var sb = new StringBuilder();
        assertThrows(IllegalArgumentException.class,
                () -> render("notch_render/hello.notch", sb, "name"));
    }
}
