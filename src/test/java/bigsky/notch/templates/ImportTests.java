package bigsky.notch.templates;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ImportTests extends NotchTemplateTestBase {

    // ========================================================================
    // Basic Import Functionality
    // ========================================================================

    @Nested
    class BasicImport {

        @Test
        void importSingleFragment() {
            var library = """
                    #macro card(id)
                        <div class="card">${id}</div>
                    #end
                    """;

            var main = """
                    #import "library.html"
                    #expand card(123)
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertEquals("""
                        <div class="card">123</div>
                    """, result);
        }

        @Test
        void importMultipleFragments() {
            var library = """
                    #macro card(id)
                        Card ${id}
                    #end

                    #macro button(label)
                        Button: ${label}
                    #end

                    #macro header(title)
                        Header: ${title}
                    #end
                    """;

            var main = """
                    #import "library.html"
                    #expand card(1)
                    #expand button("Click")
                    #expand header("Home")
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertEquals("""
                        Card 1
                        Button: Click
                        Header: Home
                    """, result);
        }

        @Test
        void importFragmentInLoop() {
            var library = """
                    #macro item(n)
                        Item #${n}
                    #end
                    """;

            var main = """
                    #import "library.html"
                    #for i in [1, 2, 3]
                        #expand item(i)
                    #end
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertEquals("""
                        Item #1
                        Item #2
                        Item #3
                    """, result);
        }

        @Test
        void importFragmentWithMultipleParameters() {
            var library = """
                    #macro person(name, age)
                        ${name} is ${age} years old
                    #end
                    """;

            var main = """
                    #import "library.html"
                    #expand person("Alice", 30)
                    #expand person("Bob", 25)
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertTrue(result.contains("Alice is 30 years old"));
            assertTrue(result.contains("Bob is 25 years old"));
        }
    }

    // ========================================================================
    // Import with Alias/Namespace
    // ========================================================================

    @Nested
    class ImportWithAlias {

        @Test
        void importWithAlias() {
            var library = """
                    #macro card(id)
                        <div>${id}</div>
                    #end
                    """;

            var main = """
                    #import "library.html" as lib
                    #expand lib.card(456)
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertEquals("""
                        <div>456</div>
                    """, result);
        }

        @Test
        void importMultipleLibrariesWithAliases() {
            var cards = """
                    #macro card(id)
                        Card ${id}
                    #end
                    """;

            var buttons = """
                    #macro button(label)
                        Button: ${label}
                    #end
                    """;

            var main = """
                    #import "cards.html" as cards
                    #import "buttons.html" as btns
                    #expand cards.card(1)
                    #expand btns.button("OK")
                    """;

            registerTemplate("cards.html", cards);
            registerTemplate("buttons.html", buttons);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertTrue(result.contains("Card 1"));
            assertTrue(result.contains("Button: OK"));
            // Verify both fragments expanded correctly
            assertEquals(1, result.split("Card 1").length - 1, "Card should expand once");
            assertEquals(1, result.split("Button: OK").length - 1, "Button should expand once");
            // Verify ordering
            assertTrue(result.indexOf("Card") < result.indexOf("Button"), "Card should come before Button");
            // Verify no namespace artifacts
            assertFalse(result.contains("cards."), "Namespace should not appear in output");
            assertFalse(result.contains("btns."), "Namespace should not appear in output");
        }

        @Test
        void importMultipleFragmentsWithAlias() {
            var library = """
                    #macro foo
                        Foo!
                    #end

                    #macro bar
                        Bar!
                    #end
                    """;

            var main = """
                    #import "library.html" as lib
                    #expand lib.foo()
                    #expand lib.bar()
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertTrue(result.contains("Foo!"));
            assertTrue(result.contains("Bar!"));
        }
    }

    // ========================================================================
    // Name Collision Handling
    // ========================================================================

    @Nested
    class NameCollisions {

        @Test
        void importedFragmentOverridesLocal() {
            var library = """
                    #macro card
                        Imported card
                    #end
                    """;

            var main = """
                    #macro card
                        Local card
                    #end

                    #import "library.html"
                    #expand card()
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            // Local fragment is defined during render, import happens in preRender
            // So local fragment is available first
            assertTrue(result.contains("Local card"));
            // Verify imported fragment is NOT used
            assertFalse(result.contains("Imported card"), "Local fragment should take precedence");
            // Verify only one expansion
            assertEquals(1, result.split("card").length - 1, "Fragment should expand once");
        }

        @Test
        void aliasedImportDoesNotConflictWithLocal() {
            var library = """
                    #macro card
                        Imported card
                    #end
                    """;

            var main = """
                    #macro card
                        Local card
                    #end

                    #import "library.html" as lib
                    #expand card()
                    #expand lib.card()
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertTrue(result.contains("Local card"));
            assertTrue(result.contains("Imported card"));
            // Verify both fragments expanded once each
            assertEquals(1, result.split("Local card").length - 1, "Local fragment should expand once");
            assertEquals(1, result.split("Imported card").length - 1, "Imported fragment should expand once");
            // Verify ordering - local should come first
            assertTrue(result.indexOf("Local") < result.indexOf("Imported"), "Local should come before Imported");
        }

        @Test
        void multipleImportsWithSameFragmentName() {
            var lib1 = """
                    #macro item
                        From lib1
                    #end
                    """;

            var lib2 = """
                    #macro item
                        From lib2
                    #end
                    """;

            var main = """
                    #import "lib1.html"
                    #import "lib2.html"
                    #expand item()
                    """;

            registerTemplate("lib1.html", lib1);
            registerTemplate("lib2.html", lib2);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            // Last import wins
            assertTrue(result.contains("From lib2"));
            // Verify first import was overridden
            assertFalse(result.contains("From lib1"), "First import should be overridden by second");
            // Verify only one expansion
            long fromCount = result.lines().filter(line -> line.contains("From")).count();
            assertEquals(1, fromCount, "Should only have one fragment expansion");
        }
    }

    // ========================================================================
    // Error Cases
    // ========================================================================

    @Nested
    class ErrorCases {

        @Test
        void importNonExistentTemplate() {
            var main = """
                    #import "missing.html"
                    Content
                    """;

            registerTemplate("main.html", main);
            assertThrows(Exception.class, () -> {
                renderTemplate("main.html");
            });
        }

        @Test
        void expandUndefinedImportedFragment() {
            var library = """
                    #macro foo
                        Foo
                    #end
                    """;

            var main = """
                    #import "library.html"
                    #expand bar()
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            assertThrows(Exception.class, () -> {
                renderTemplateForError("main.html");
            });
        }

        @Test
        void importWithInvalidAliasSyntax() {
            var library = """
                    #macro foo
                        Foo
                    #end
                    """;

            var main = """
                    #import "library.html" as
                    #expand foo()
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            assertThrows(Exception.class, () -> {
                renderTemplateForError("main.html");
            });
        }

        @Test
        void importWithoutPath() {
            var main = """
                    #import
                    Content
                    """;

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
        void nestedFragmentExpansion() {
            var library = """
                    #macro outer(x)
                        Outer: ${x}
                        #macro inner(y)
                            Inner: ${y}
                        #end
                        #expand inner(x)
                    #end
                    """;

            var main = """
                    #import "library.html"
                    #expand outer(42)
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertTrue(result.contains("Outer: 42"));
            assertTrue(result.contains("Inner: 42"));
            // Verify nesting structure
            assertTrue(result.indexOf("Outer:") < result.indexOf("Inner:"), "Outer should come before Inner");
            // Verify parameter passing
            assertFalse(result.contains("${"), "All variables should be interpolated");
            // Verify both fragments expanded once
            assertEquals(1, result.split("Outer: 42").length - 1, "Outer should expand once");
            assertEquals(1, result.split("Inner: 42").length - 1, "Inner should expand once");
        }

        @Test
        void importedFragmentCallingAnotherImportedFragment() {
            var library = """
                    #macro base(n)
                        Base: ${n}
                    #end

                    #macro wrapper(x)
                        Wrapper: ${x}
                        #expand base(x)
                    #end
                    """;

            var main = """
                    #import "library.html"
                    #expand wrapper(100)
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertTrue(result.contains("Wrapper: 100"));
            assertTrue(result.contains("Base: 100"));
        }

        @Test
        void importInsideConditional() {
            var library = """
                    #macro msg
                        Hello from library
                    #end
                    """;

            var main = """
                    #if true
                        #import "library.html"
                    #end
                    #expand msg()
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            // Import is Global, runs in preRender regardless of conditionals
            assertTrue(result.contains("Hello from library"));
        }

        @Test
        void multipleImportsFromSameTemplate() {
            var library = """
                    #macro item
                        Item content
                    #end
                    """;

            var main = """
                    #import "library.html"
                    #import "library.html" as lib
                    #expand item()
                    #expand lib.item()
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            // Should work fine, imported twice
            long count = result.lines().filter(line -> line.contains("Item content")).count();
            assertEquals(2, count);
        }

        @Test
        void importLibraryWithNoFragments() {
            var library = """
                    Just some text, no fragments
                    """;

            var main = """
                    #import "library.html"
                    No fragments imported
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            // Should not fail, just no fragments available
            assertTrue(result.contains("No fragments imported"));
        }

        @Test
        void importWithVariablesInScope() {
            var library = """
                    #macro greet(name)
                        Hello ${name}, you have ${count} messages
                    #end
                    """;

            var main = """
                    #import "library.html"
                    #expand greet("Alice")
                    """;

            registerTemplate("library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html", "count", 5);
            assertTrue(result.contains("Hello Alice"));
            assertTrue(result.contains("you have 5 messages"));
        }
    }

    // ========================================================================
    // Import from Nested Directories
    // ========================================================================

    @Nested
    class ImportPaths {

        @Test
        void importFromSubdirectory() {
            var library = """
                    #macro widget
                        Widget from components
                    #end
                    """;

            var main = """
                    #import "components/library.html"
                    #expand widget()
                    """;

            registerTemplate("components/library.html", library);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertTrue(result.contains("Widget from components"));
            // Verify path handling worked correctly
            assertFalse(result.contains("components/"), "Path should not appear in output");
            assertFalse(result.contains("#import"), "Import commands should not appear in output");
            // Verify complete expansion
            assertEquals(1, result.split("Widget from components").length - 1, "Widget should expand once");
        }

        @Test
        void importMultipleFromDifferentPaths() {
            var widgets = """
                    #macro widget
                        Widget
                    #end
                    """;

            var layouts = """
                    #macro layout
                        Layout
                    #end
                    """;

            var main = """
                    #import "components/widgets.html" as w
                    #import "layouts/base.html" as l
                    #expand w.widget()
                    #expand l.layout()
                    """;

            registerTemplate("components/widgets.html", widgets);
            registerTemplate("layouts/base.html", layouts);
            registerTemplate("main.html", main);
            var result = renderTemplate("main.html");
            assertTrue(result.contains("Widget"));
            assertTrue(result.contains("Layout"));
            // Verify both fragments expanded
            assertEquals(1, result.split("Widget").length - 1, "Widget should expand once");
            assertEquals(1, result.split("Layout").length - 1, "Layout should expand once");
            // Verify ordering
            assertTrue(result.indexOf("Widget") < result.indexOf("Layout"), "Widget should come before Layout");
            // Verify no path or namespace artifacts
            assertFalse(result.contains("components/"), "Paths should not appear in output");
            assertFalse(result.contains("layouts/"), "Paths should not appear in output");
            assertFalse(result.contains("w."), "Namespace should not appear in output");
            assertFalse(result.contains("l."), "Namespace should not appear in output");
        }
    }
}
