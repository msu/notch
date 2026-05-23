package edu.montana.notch.templates;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

public class IncludeTests extends NotchTemplateTestBase {

    // ========================================================================
    // Basic Include Functionality
    // ========================================================================

    @Nested
    class BasicInclude {

        @Test
        void simpleInclude() {
            var partial = "Hello from partial!";

            var main = """
                    Before include
                    #include "partial.html"
                    After include
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("Before include", result);
            assertContains("Hello from partial!", result);
            assertContains("After include", result);
            // Verify ordering
            assertTrue(result.indexOf("Before") < result.indexOf("Hello from partial"), "Before should come first");
            assertTrue(result.indexOf("Hello from partial") < result.indexOf("After"), "Partial should be in middle");
            // Verify no commands in output
            assertFalse(result.contains("#include"), "Include commands should not appear in output");
        }

        @Test
        void includeWithHTML() {
            var header = """
                    <header>
                        <h1>My Website</h1>
                    </header>
                    """;

            var main = """
                    <!DOCTYPE html>
                    <html>
                    #include "header.html"
                    <body>Content</body>
                    </html>
                    """;

            registerTemplate("header.html", header);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("<header>", result);
            assertContains("<h1>My Website</h1>", result);
            // Verify proper HTML structure
            assertContains("<!DOCTYPE html>", result, "Doctype should be present");
            assertContains("</header>", result, "Closing header tag should be present");
            // Verify include is positioned correctly in HTML
            assertTrue(result.indexOf("<!DOCTYPE") < result.indexOf("<header>"), "Header should be after doctype");
            assertTrue(result.indexOf("</header>") < result.indexOf("<body>"), "Header should be before body");
        }

        @Test
        void includeMultipleTemplates() {
            var header = "<header>Header</header>";
            var footer = "<footer>Footer</footer>";

            var main = """
                    #include "header.html"
                    <main>Content</main>
                    #include "footer.html"
                    """;

            registerTemplate("header.html", header);
            registerTemplate("footer.html", footer);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("<header>Header</header>", result);
            assertContains("<footer>Footer</footer>", result);
            // Verify ordering
            assertTrue(result.indexOf("<header>") < result.indexOf("<main>"), "Header should come before main");
            assertTrue(result.indexOf("<main>") < result.indexOf("<footer>"), "Main should come before footer");
            // Verify all three sections present
            assertContains("Content", result, "Main content should be present");
            // Verify each element appears once
            assertEquals(1, result.split("<header>").length - 1, "Header should appear once");
            assertEquals(1, result.split("<footer>").length - 1, "Footer should appear once");
        }

        @Test
        void includeSameTemplateMultipleTimes() {
            var divider = "<hr />";

            var main = """
                    Section 1
                    #include "divider.html"
                    Section 2
                    #include "divider.html"
                    Section 3
                    """;

            registerTemplate("divider.html", divider);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            long count = result.lines().filter(line -> line.contains("<hr />")).count();
            assertEquals(2, count);
            // Verify all three sections present
            assertContains("Section 1", result, "Section 1 should be present");
            assertContains("Section 2", result, "Section 2 should be present");
            assertContains("Section 3", result, "Section 3 should be present");
            // Verify dividers are between sections
            int firstHr = result.indexOf("<hr />");
            int secondHr = result.lastIndexOf("<hr />");
            assertNotEquals(firstHr, secondHr, "Should have two different divider positions");
            assertTrue(firstHr < result.indexOf("Section 2"), "First divider before Section 2");
            assertTrue(secondHr < result.indexOf("Section 3"), "Second divider before Section 3");
        }
    }

    // ========================================================================
    // Variable Scope Inheritance
    // ========================================================================

    @Nested
    class ScopeInheritance {

        @Test
        void includeInheritsVariables() {
            var partial = "Hello ${name}!";

            var main = """
                    #include "partial.html"
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html", "name", "World");
            assertContains("Hello World!", result);
            // Verify variable was interpolated
            assertFalse(result.contains("${"), "Variable should be interpolated");
            assertFalse(result.contains("name"), "Variable name should be replaced");
            // Verify exact content
            assertTrue(result.trim().equals("Hello World!"), "Should match exact content");
        }

        @Test
        void includeInheritsMultipleVariables() {
            var partial = "${greeting} ${name}, you are ${age} years old";

            var main = """
                    #include "partial.html"
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html",
                    "greeting", "Hi",
                    "name", "Alice",
                    "age", 30
            );
            assertContains("Hi Alice, you are 30 years old", result);
        }

        @Test
        void includeWithLoopVariable() {
            var partial = "Item: ${item}";

            var main = """
                    #for item in [1, 2, 3]
                        #include "partial.html"
                    #end
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("Item: 1", result);
            assertContains("Item: 2", result);
            assertContains("Item: 3", result);
            // Verify count and ordering
            assertEquals(3, result.split("Item:").length - 1, "Should have 3 items");
            assertTrue(result.indexOf("Item: 1") < result.indexOf("Item: 2"), "Items should be in order");
            assertTrue(result.indexOf("Item: 2") < result.indexOf("Item: 3"), "Items should be in order");
            // Verify no unresolved variables
            assertFalse(result.contains("${"), "All variables should be interpolated");
        }

        @Test
        void includeWithIndexVariable() {
            var partial = "Index: ${index}, Value: ${val}";

            var main = """
                    #for val in ["a", "b", "c"]
                        #include "partial.html"
                    #end
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("Index: 0, Value: a", result);
            assertContains("Index: 1, Value: b", result);
            assertContains("Index: 2, Value: c", result);
        }

        @Test
        void includeDoesNotPolluteScopeBack() {
            // Variables defined in included template should not affect parent scope
            var partial = """
                    #for x in [1, 2, 3]
                        ${x}
                    #end
                    """;

            var main = """
                    #include "partial.html"
                    Value of x: ${x ?: "undefined"}
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            // x should be undefined in main after include
            assertContains("Value of x: undefined", result);
        }
    }

    // ========================================================================
    // Nested Includes
    // ========================================================================

    @Nested
    class NestedIncludes {

        @Test
        void includeIncludingAnother() {
            var innermost = "Innermost content";
            var middle = """
                    Middle start
                    #include "innermost.html"
                    Middle end
                    """;
            var outer = """
                    Outer start
                    #include "middle.html"
                    Outer end
                    """;

            registerTemplate("innermost.html", innermost);
            registerTemplate("middle.html", middle);
            registerTemplate("outer.html", outer);
            var result = renderTemplate("outer.html");
            assertContains("Outer start", result);
            assertContains("Middle start", result);
            assertContains("Innermost content", result);
            assertContains("Middle end", result);
            assertContains("Outer end", result);
            // Verify proper nesting order
            assertTrue(result.indexOf("Outer start") < result.indexOf("Middle start"), "Outer should start first");
            assertTrue(result.indexOf("Middle start") < result.indexOf("Innermost"), "Middle should start before innermost");
            assertTrue(result.indexOf("Innermost") < result.indexOf("Middle end"), "Innermost should be in middle");
            assertTrue(result.indexOf("Middle end") < result.indexOf("Outer end"), "Middle should end before outer");
            // Verify no include commands in output
            assertFalse(result.contains("#include"), "Include commands should not appear in output");
        }

        @Test
        void deeplyNestedIncludes() {
            var level5 = "Level 5";
            var level4 = """
                    L4
                    #include "level5.html"
                    """;
            var level3 = """
                    L3
                    #include "level4.html"
                    """;
            var level2 = """
                    L2
                    #include "level3.html"
                    """;
            var level1 = """
                    L1
                    #include "level2.html"
                    """;

            registerTemplate("level5.html", level5);
            registerTemplate("level4.html", level4);
            registerTemplate("level3.html", level3);
            registerTemplate("level2.html", level2);
            registerTemplate("level1.html", level1);
            var result = renderTemplate("level1.html");
            assertContains("L1", result);
            assertContains("L2", result);
            assertContains("L3", result);
            assertContains("L4", result);
            assertContains("Level 5", result);
        }

        @Test
        void nestedIncludesWithVariables() {
            var inner = "Inner: ${value}";
            var outer = """
                    Outer: ${value}
                    #include "inner.html"
                    """;

            registerTemplate("inner.html", inner);
            registerTemplate("outer.html", outer);
            var result = renderTemplate("outer.html", "value", 42);
            assertContains("Outer: 42", result);
            assertContains("Inner: 42", result);
        }
    }

    // ========================================================================
    // Include in Control Structures
    // ========================================================================

    @Nested
    class IncludeInControlStructures {

        @Test
        void includeInIfStatement() {
            var partial = "Conditional content";

            var main = """
                    #if show
                        #include "partial.html"
                    #end
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);

            // Test when condition is true
            var resultTrue = renderTemplate("main.html", "show", true);
            assertContains("Conditional content", resultTrue);

            // Test when condition is false
            var resultFalse = renderTemplate("main.html", "show", false);
            assertFalse(resultFalse.contains("Conditional content"));
        }

        @Test
        void includeInElseBranch() {
            var partial = "From else";

            var main = """
                    #if false
                        From if
                    #else
                        #include "partial.html"
                    #end
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("From else", result);
            assertFalse(result.contains("From if"));
        }

        @Test
        void includeInLoop() {
            var item = "- Item ${i}";

            var main = """
                    #for i in [1, 2, 3]
                        #include "item.html"
                    #end
                    """;

            registerTemplate("item.html", item);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("- Item 1", result);
            assertContains("- Item 2", result);
            assertContains("- Item 3", result);
            // Verify count
            assertEquals(3, result.split("- Item").length - 1, "Should have 3 items");
            // Verify ordering
            assertTrue(result.indexOf("Item 1") < result.indexOf("Item 2"), "Items should be in order");
            assertTrue(result.indexOf("Item 2") < result.indexOf("Item 3"), "Items should be in order");
            // Verify no commands in output
            assertFalse(result.contains("#for"), "Loop commands should not appear in output");
            assertFalse(result.contains("#include"), "Include commands should not appear in output");
        }

        @Test
        void includeInNestedLoops() {
            var cell = "${row}x${col}";

            var main = """
                    #for row in [1, 2]
                        #for col in [1, 2, 3]
                            #include "cell.html"
                        #end
                    #end
                    """;

            registerTemplate("cell.html", cell);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("1x1", result);
            assertContains("1x2", result);
            assertContains("2x3", result);
        }
    }

    // ========================================================================
    // Include with Fragments
    // ========================================================================

    @Nested
    class IncludeWithFragments {

        @Test
        void includeTemplateWithFragments() {
            var partial = """
                    #macro item(n)
                        Item ${n}
                    #end
                    
                    #expand item(1)
                    #expand item(2)
                    """;

            var main = """
                    #include "partial.html"
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("Item 1", result);
            assertContains("Item 2", result);
            // Verify both items expanded
            assertEquals(2, result.split("Item").length - 1, "Should have 2 items");
            // Verify ordering
            assertTrue(result.indexOf("Item 1") < result.indexOf("Item 2"), "Items should be in order");
            // Verify no fragment commands in output
            assertFalse(result.contains("#fragment"), "Fragment commands should not appear in output");
            assertFalse(result.contains("#expand"), "Expand commands should not appear in output");
        }

        @Test
        void includeCanUseParentFragments() {
            var partial = """
                    #expand card(100)
                    """;

            var main = """
                    #macro card(id)
                        Card ${id}
                    #end
                    
                    #include "partial.html"
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("Card 100", result);
        }

        @Test
        void includeTemplateDefinesFragmentUsedLater() {
            var partial = """
                    #macro helper(msg)
                        Helper: ${msg}
                    #end
                    """;

            var main = """
                    #include "partial.html"
                    #expand helper("test")
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            // Fragment defined in included template is available after include
            assertContains("Helper: test", result);
        }
    }

    // ========================================================================
    // Error Cases
    // ========================================================================

    @Nested
    class ErrorCases {

        @Test
        void includeNonExistentTemplate() {
            var main = """
                    #include "missing.html"
                    """;

            registerTemplate("main.html", main);
            assertThrows(Exception.class, () -> {
                renderTemplateForError("main.html");
            });
        }

        @Test
        void includeWithoutPath() {
            var main = """
                    #include
                    """;

            registerTemplate("main.html", main);
            assertThrows(Exception.class, () -> {
                renderTemplateForError("main.html");
            });
        }

        @Test
        void includeWithExtraArguments() {
            var partial = "Content";

            var main = """
                    #include "partial.html" extra stuff
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            assertThrows(Exception.class, () -> {
                renderTemplateForError("main.html");
            });
        }
    }

    // ========================================================================
    // Complex Scenarios
    // ========================================================================

    @Nested
    class ComplexScenarios {

        @Test
        void includeWithConditionalContent() {
            var partial = """
                    #if admin
                        Admin panel
                    #else
                        User panel
                    #end
                    """;

            var main = """
                    #include "partial.html"
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);

            var resultAdmin = renderTemplate("main.html", "admin", true);
            assertContains("Admin panel", resultAdmin);
            assertFalse(resultAdmin.contains("User panel"), "User panel should not show for admin");
            assertFalse(resultAdmin.contains("#if"), "Commands should not appear in output");

            var resultUser = renderTemplate("main.html", "admin", false);
            assertContains("User panel", resultUser);
            assertFalse(resultUser.contains("Admin panel"), "Admin panel should not show for user");
            assertFalse(resultUser.contains("#if"), "Commands should not appear in output");
        }

        @Test
        void includeWithLoopInPartial() {
            var partial = """
                    #for item in items
                        - ${item}
                    #end
                    """;

            var main = """
                    #include "partial.html"
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html", "items", java.util.List.of("A", "B", "C"));
            assertContains("- A", result);
            assertContains("- B", result);
            assertContains("- C", result);
        }

        @Test
        void includePartialMultipleTimesWithDifferentVariables() {
            var partial = "Name: ${name}";

            var main = """
                    #for name in ["Alice", "Bob", "Charlie"]
                        #include "partial.html"
                    #end
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("Name: Alice", result);
            assertContains("Name: Bob", result);
            assertContains("Name: Charlie", result);
        }

        @Test
        void includeWithXSSProtection() {
            var partial = "User: ${username}";

            var main = """
                    #include "partial.html"
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html", "username", "<script>alert('xss')</script>");
            // Should be escaped
            assertContains("&lt;script&gt;", result);
            assertFalse(result.contains("<script>"));
            // Verify full escaping
            assertContains("&lt;/script&gt;", result, "Closing script tag should be escaped");
            assertContains("&apos;", result, "Single quotes should be escaped");
            // Verify no executable script remains
            assertFalse(result.matches(".*<script[^>]*>.*"), "No executable script tags");
        }

        @Test
        void includeChainWithDifferentPaths() {
            var components = "Component content";
            var layouts = """
                    Layout:
                    #include "components/widget.html"
                    """;

            var main = """
                    #include "layouts/base.html"
                    """;

            registerTemplate("components/widget.html", components);
            registerTemplate("layouts/base.html", layouts);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertContains("Layout:", result);
            assertContains("Component content", result);
        }
    }

    // ========================================================================
    // Performance Scenarios
    // ========================================================================

    @Nested
    class PerformanceScenarios {

        @Test
        void includeManyTimesInLoop() {
            var partial = ".";

            var main = """
                    #for i in items
                        #include "partial.html"
                    #end
                    """;

            var items = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                items.add(i);
            }

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html", "items", items);
            // Should render 100 dots
            long count = result.chars().filter(ch -> ch == '.').count();
            assertEquals(100, count);
            // Verify no commands in output
            assertFalse(result.contains("#for"), "Loop commands should not appear in output");
            assertFalse(result.contains("#include"), "Include commands should not appear in output");
            // Verify result length is reasonable (100 dots + some whitespace)
            assertTrue(result.length() >= 100, "Should have at least 100 characters");
        }

        @Test
        void includeWithManyVariables() {
            var partial = "${v1} ${v2} ${v3} ${v4} ${v5}";

            var main = """
                    #include "partial.html"
                    """;

            registerTemplate("partial.html", partial);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html",
                    "v1", 1, "v2", 2, "v3", 3, "v4", 4, "v5", 5
            );
            assertContains("1 2 3 4 5", result);
        }
    }
}
