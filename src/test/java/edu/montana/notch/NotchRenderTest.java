package edu.montana.notch;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

public class NotchRenderTest {

    @Test
    public void renderWithNoVars() {
        String result = Notch.render("notch_render/static.notch");
        assertEquals("static content", result);
    }

    @Test
    public void renderWithSingleVar() {
        String result = Notch.render("notch_render/hello.notch", "name", "Alice");
        assertEquals("Hello, Alice!", result);
    }

    @Test
    public void renderWithMultipleVars() {
        String result = Notch.render("notch_render/greet.notch", "greeting", "Howdy", "name", "Bob");
        assertEquals("Howdy, Bob!", result);
    }

    @Test
    public void renderEscapesHtmlByDefault() {
        String result = Notch.render("notch_render/hello.notch", "name", "<script>");
        assertEquals("Hello, &lt;script&gt;!", result);
    }

    @Test
    public void renderToStringBuilder() {
        var sb = new StringBuilder("[prefix]");
        Notch.render("notch_render/hello.notch", sb, "name", "Carol");
        assertEquals("[prefix]Hello, Carol!", sb.toString());
    }

    @Test
    public void renderToStringWriter() {
        var sw = new StringWriter();
        Notch.render("notch_render/greet.notch", sw, "greeting", "Hi", "name", "Dave");
        assertEquals("Hi, Dave!", sw.toString());
    }

    @Test
    public void renderToAppendableWithNoVars() {
        var sb = new StringBuilder();
        Notch.render("notch_render/static.notch", sb);
        assertEquals("static content", sb.toString());
    }

    @Test
    public void renderOddArityThrows() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> Notch.render("notch_render/hello.notch", "name"));
        assertTrue(ex.getMessage().contains("even number"), ex.getMessage());
    }

    @Test
    public void renderNonStringKeyThrows() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> Notch.render("notch_render/hello.notch", 42, "Alice"));
        assertTrue(ex.getMessage().contains("must be a String"), ex.getMessage());
    }

    @Test
    public void renderMissingTemplateThrows() {
        assertThrows(RuntimeException.class,
                () -> Notch.render("notch_render/does_not_exist.notch"));
    }

    @Test
    public void renderAppendableOddArityThrows() {
        var sb = new StringBuilder();
        assertThrows(IllegalArgumentException.class,
                () -> Notch.render("notch_render/hello.notch", sb, "name"));
    }
}
