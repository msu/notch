package edu.montana.notch.templates;


import edu.montana.notch.chisel.Source;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.templates.loader.NotchTemplateLoader;
import edu.montana.notch.templates.runtime.NotchTemplateHelper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static edu.montana.notch.AssertContains.assertContains;
import static edu.montana.notch.util.Text.repr;
import static org.junit.jupiter.api.Assertions.*;

public class TemplateTests extends NotchTemplateTestBase {
    @Test
    public void testRawContent() {
        var tmpl = """
                Hello, World
                """;

        var content = renderString(tmpl);
        assertEquals("Hello, World\n", content);
        // Verify no interpolation occurred
        assertFalse(content.contains("${"));
        // Verify exact length
        assertEquals(13, content.length());
    }

    @Test
    public void testContent() {
        var tmpl = """
                chicken noodle ${name}
                """;

        var content = renderString(tmpl, "name", "soup");
        assertEquals("chicken noodle soup\n", content);
    }

    @Test
    public void testUnsetFallback() {
        var tmpl = """
                ${value?: "so back"}
                """;
        var content = renderString(tmpl);
        assertEquals("so back\n", content);
    }

    @Test
    public void testConditionalExpr() {
        var tmpl = """
                ${"goodbye" if false}
                ${"hello" if true}
                """;
        var content = renderString(tmpl);
        assertEquals("\nhello\n", content);
        // Verify false condition produces empty output
        assertFalse(content.contains("goodbye"), "False condition should not render");
        // Verify true condition produces output
        assertContains("hello", content, "True condition should render");
        // Verify line structure
        assertEquals(2, content.lines().count(), "Should have exactly 2 lines");
    }

    public static class BasicHelper implements NotchTemplateHelper {
        public BasicHelper() {
        }

        @Override
        public Object resolveSymbol(String name) {
            if (name.equals("name")) return "friend";
            return NotchRuntime.UNDEFINED;
        }
    }

    @Test
    public void testHelper() {
        assertEquals("edu.montana.notch.templates.TemplateTests$BasicHelper", BasicHelper.class.getName());
        var tmpl = """
                #helper edu.montana.notch.templates.TemplateTests$BasicHelper
                Hello, ${name}
                """;
        var content = renderString(tmpl);
        assertEquals("Hello, friend\n", content);
        // Verify helper resolved the variable
        assertContains("friend", content, "Helper should resolve 'name' to 'friend'");
        assertFalse(content.contains("${"), "No unresolved variables");
        assertFalse(content.contains("#helper"), "Command should not appear in output");
    }

    @Test
    public void testLayout() {
        var base = """
                <div class="wrapper">
                #content
                </div>
                
                <footer>
                #content footer with
                    Goodbye!
                #end
                </footer>
                """;

        var tmpl = """
                #layout "base.html"
                
                Hello, World
                
                #content footer with
                    Goodbye, World
                #end
                """;

        var templates = new NotchTemplates(new NotchTemplateLoader() {
            @Override
            public Source loadSource(String path) {
                if (path.equals("base.html")) return new Source("base.html", base);
                if (path.equals("index.html")) return new Source("index.html", tmpl);
                throw new RuntimeException("unknown path " + repr(path));
            }
        });
        BasicNotchTemplateCommands.addTo(templates);

        var content = templates.renderTemplate("index.html", Map.of());
        assertEquals("""
                <div class="wrapper">
                
                Hello, World
                
                </div>
                
                <footer>
                    Goodbye, World
                </footer>
                """, content);
    }

    @Test
    public void testConditional() {
        var tmpl = """
                #if true
                    Yay it's true
                #end
                #if false
                #else
                    Boo it's false
                #end
                #if false
                #end
                """;

        var result = renderString(tmpl);
        assertEquals("""
                    Yay it's true
                    Boo it's false
                """, result);
        // Verify commands not in output
        assertFalse(result.contains("#if"), "Commands should not appear in output");
        assertFalse(result.contains("#end"), "Commands should not appear in output");
        assertFalse(result.contains("#else"), "Commands should not appear in output");
        // Verify line count
        assertEquals(2, result.lines().count(), "Should have exactly 2 content lines");
    }

    @Test
    public void testChainedConditional() {
        var tmpl = """
                #if x == 1
                    1
                #elseif x == 2
                    2
                #else
                    3
                #end
                """;

        var result = renderString(tmpl, "x", 1);
        assertEquals("""
                    1
                """, result);

        result = renderString(tmpl, "x", 2);
        assertEquals("""
                    2
                """, result);

        result = renderString(tmpl, "x", 3);
        assertEquals("""
                    3
                """, result);

        result = renderString(tmpl, "x", 4);
        assertEquals("""
                    3
                """, result);
    }

    @Test
    public void testForElse() {
        var tmpl = """
                #for x in []
                #else
                Hi!
                #end
                """;
        var result = renderString(tmpl);
        assertEquals("Hi!\n", result);
        // Verify else block executed
        assertContains("Hi!", result, "Else block should execute for empty list");
        // Verify no loop iterations occurred
        assertEquals(1, result.lines().count(), "Should have exactly 1 line from else block");
        assertFalse(result.contains("#"), "No commands in output");
    }

    @Test
    public void crossSiteScripting() {
        var template = """
                Hello, welcome to ${msg}
                """;

        var output = renderString(template, "msg", """
                <script>alert('pwn!!');</alert>
                """);

        assertEquals("Hello, welcome to &lt;script&gt;alert(&apos;pwn!!&apos;);&lt;/alert&gt;\n\n", output);
        // Verify dangerous characters are escaped
        assertFalse(output.contains("<script>"), "Script tags should be escaped");
        assertContains("&lt;script&gt;", output, "Should contain escaped script tag");
        assertContains("&apos;", output, "Single quotes should be escaped");
        // Verify no actual executable script
        assertFalse(output.matches(".*<script[^>]*>.*"), "No executable script tags");
    }

    @Test
    public void simpleMacroCommand() {
        var tmpl = """
                #macro card
                <div class="card">
                    I am a Card!!
                </div>
                #end
                
                #for i in [1, 2, 3]
                    #expand card(i)
                #end
                """;
        var result = renderString(tmpl);
        assertEquals("\n" + """
                <div class="card">
                    I am a Card!!
                </div>
                """.repeat(3), result);
        // Verify fragment was expanded 3 times
        assertEquals(3, result.split("I am a Card!!").length - 1, "Macro should expand 3 times");
        // Verify HTML structure
        assertEquals(3, result.split("<div class=\"card\">").length - 1, "Should have 3 card divs");
        assertEquals(3, result.split("</div>").length - 1, "Should have 3 closing divs");
        // Verify commands not in output
        assertFalse(result.contains("#macro"), "No fragment commands in output");
        assertFalse(result.contains("#expand"), "No expand commands in output");
    }

    @Test
    public void macroArguments() {
        var tmpl = """
                #macro card(id)
                <div class="card">
                    I am Card #${id}!!
                </div>
                #end
                
                #for i in [1, 2, 3]
                    #expand card(i)
                #end
                """;
        var result = renderString(tmpl);
        assertEquals("""
                
                <div class="card">
                    I am Card #1!!
                </div>
                <div class="card">
                    I am Card #2!!
                </div>
                <div class="card">
                    I am Card #3!!
                </div>
                """, result);
        // Verify each card has unique ID
        assertContains("Card #1!!", result, "Should have card with ID 1");
        assertContains("Card #2!!", result, "Should have card with ID 2");
        assertContains("Card #3!!", result, "Should have card with ID 3");
        // Verify correct ordering
        assertTrue(result.indexOf("Card #1") < result.indexOf("Card #2"), "Card 1 should come before Card 2");
        assertTrue(result.indexOf("Card #2") < result.indexOf("Card #3"), "Card 2 should come before Card 3");
        // Verify all arguments were interpolated
        assertFalse(result.contains("${"), "All variables should be interpolated");
    }
}
