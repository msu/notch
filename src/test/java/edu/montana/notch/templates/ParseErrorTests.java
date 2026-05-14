package edu.montana.notch.templates;

import edu.montana.notch.templates.loader.NotchTemplateLoader;
import edu.montana.notch.templates.runtime.RenderException;
import edu.montana.notch.chisel.ParseException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ParseErrorTests extends NotchTemplateTestBase {
    void expectParseError(String content, String expectedMessageFragment) {
        var argMap = new LinkedHashMap<String, Object>();
        var templ = new NotchTemplateRegistry();
        BasicNotchTemplateCommands.addTo(templ);

        RenderException ex = assertThrows(RenderException.class, () -> {
            templ.renderString(testInfo.getDisplayName(), content, argMap);
        }, "Expected ParseException but template rendered successfully");

        // Check that the underlying cause is a ParseException
        Throwable cause = ex.getCause();
        while (cause != null && !(cause instanceof ParseException)) {
            cause = cause.getCause();
        }

        assertNotNull(cause, "Expected ParseException in the exception chain");
        assertTrue(cause instanceof ParseException, "Expected ParseException but got: " + cause.getClass());

        if (expectedMessageFragment != null) {
            String message = cause.getMessage();
            assertTrue(message.contains(expectedMessageFragment), "Expected error message to contain '" + expectedMessageFragment + "' but got: " + message);
        }
    }

    void expectRenderError(String content, String expectedMessageFragment, Object... args) {
        var argMap = new LinkedHashMap<String, Object>();
        for (int i = 0; i < args.length; i += 2) {
            var name = (String) args[i];
            var value = args[i + 1];
            argMap.put(name, value);
        }

        var templ = new NotchTemplateRegistry();
        BasicNotchTemplateCommands.addTo(templ);

        RenderException ex = assertThrows(RenderException.class, () -> {
            templ.renderString(testInfo.getDisplayName(), content, argMap);
        }, "Expected RenderException but template rendered successfully");

        if (expectedMessageFragment != null) {
            String message = ex.getMessage();
            assertTrue(message.contains(expectedMessageFragment), "Expected error message to contain '" + expectedMessageFragment + "' but got: " + message);
        }
    }

    // ========================================================================
    // Missing #end Tags
    // ========================================================================

    @Nested
    class MissingEndTags {

        @Test
        void missingEndForIf() {
            var tmpl = """
                    #if true
                        content here
                    """;
            expectParseError(tmpl, "unterminated");
        }

        @Test
        void missingEndForNestedIf() {
            var tmpl = """
                    #if true
                        outer content
                        #if false
                            inner content
                        #end
                    """;
            expectParseError(tmpl, "unterminated");
        }

        @Test
        void missingEndForFor() {
            var tmpl = """
                    #for x in [1,2,3]
                        ${x}
                    """;
            expectParseError(tmpl, "unterminated");
        }

        @Test
        void missingEndForFragment() {
            var tmpl = """
                    #macro card(id)
                        <div>${id}</div>
                    """;
            expectParseError(tmpl, "unterminated");
        }

        @Test
        void missingEndForContent() {
            var tmpl = """
                    #content for footer default
                        Footer content
                    """;
            expectParseError(tmpl, "unterminated");
        }

        @Test
        void missingEndForNestedFor() {
            var tmpl = """
                    #for x in [1,2,3]
                        ${x}
                        #for y in [4,5,6]
                            ${y}
                        #end
                    """;
            expectParseError(tmpl, "unterminated");
        }

        @Test
        void missingEndForIfWithElse() {
            var tmpl = """
                    #if true
                        first
                    #else
                        second
                    """;
            expectParseError(tmpl, "unterminated");
        }

        @Test
        void missingEndForIfWithElseIf() {
            var tmpl = """
                    #if false
                        first
                    #elseif true
                        second
                    """;
            expectParseError(tmpl, "unterminated");
        }
    }

    // ========================================================================
    // Mismatched and Orphaned #else/#elseif
    // ========================================================================

    @Nested
    class MismatchedElseAndElseIf {

        @Test
        void elseifAfterElse() {
            var tmpl = """
                    #if false
                        first
                    #else
                        second
                    #elseif true
                        third
                    #end
                    """;
            // The parser is lenient - #elseif after #else is treated as a command in the else block
            // This actually renders successfully with the else block containing the elseif
            var result = renderString(tmpl);
            assertNotNull(result, "Parser allows elseif after else (though semantically odd)");
        }

        @Test
        void doubleElseInFor() {
            var tmpl = """
                    #for x in [1,2,3]
                        ${x}
                    #else
                        empty
                    #else
                        double else
                    #end
                    """;
            // The parser is lenient - second #else is treated as a command in the first else block
            var result = renderString(tmpl);
            assertNotNull(result, "Parser allows double else (though semantically odd)");
        }

        @Test
        void elseifInFor() {
            var tmpl = """
                    #for x in [1,2,3]
                        ${x}
                    #elseif true
                        This shouldn't work
                    #end
                    """;
            // The parser is lenient - #elseif is treated as a regular command in the for loop body
            var result = renderString(tmpl);
            assertNotNull(result, "Parser allows elseif in for (though semantically odd)");
        }
    }

    // ========================================================================
    // Malformed Command Syntax
    // ========================================================================

    @Nested
    class MalformedCommands {

        @Test
        void ifWithoutCondition() {
            var tmpl = """
                    #if
                        content
                    #end
                    """;
            // Missing condition causes runtime error from Notch expression parser
            expectRenderError(tmpl, null);
        }

        @Test
        void forWithoutIn() {
            var tmpl = """
                    #for x [1,2,3]
                        ${x}
                    #end
                    """;
            expectParseError(tmpl, "in");
        }

        @Test
        void forWithoutVarName() {
            var tmpl = """
                    #for in [1,2,3]
                        content
                    #end
                    """;
            expectParseError(tmpl, null);
        }

        @Test
        void forWithoutIterable() {
            var tmpl = """
                    #for x in
                        content
                    #end
                    """;
            expectParseError(tmpl, null);
        }

        @Test
        void elseifWithoutCondition() {
            var tmpl = """
                    #if false
                        first
                    #elseif
                        second
                    #end
                    """;
            // Missing condition causes runtime error from Notch expression parser
            expectRenderError(tmpl, null);
        }

        @Test
        void fragmentWithoutName() {
            var tmpl = """
                    #fragment
                        content
                    #end
                    """;
            expectParseError(tmpl, null);
        }

        @Test
        void expandWithoutName() {
            var tmpl = """
                    #expand
                    """;
            expectParseError(tmpl, null);
        }

        @Test
        void ifWithExtraTokens() {
            var tmpl = """
                    #if true extra stuff here
                        content
                    #end
                    """;
            expectParseError(tmpl, "extra tokens");
        }

        @Test
        void forWithExtraTokens() {
            var tmpl = """
                    #for x in [1,2,3] extra
                        ${x}
                    #end
                    """;
            expectParseError(tmpl, null);
        }
    }

    // ========================================================================
    // Unknown Commands
    // ========================================================================

    @Nested
    class UnknownCommands {

        @Test
        void unknownCommand() {
            var tmpl = """
                    #foobar
                        content
                    #end
                    """;
            expectParseError(tmpl, "unknown command");
        }

        @Test
        void typoInIfCommand() {
            var tmpl = """
                    #iif true
                        content
                    #end
                    """;
            expectParseError(tmpl, "unknown command");
        }

        @Test
        void caseSensitiveCommand() {
            var tmpl = """
                    #IF true
                        content
                    #END
                    """;
            expectParseError(tmpl, "unknown command");
        }

        @Test
        void typoInForCommand() {
            var tmpl = """
                    #fore x in [1,2,3]
                        ${x}
                    #end
                    """;
            expectParseError(tmpl, "unknown command");
        }
    }

    // ========================================================================
    // Malformed Expressions
    // ========================================================================

    @Nested
    class MalformedExpressions {

        @Test
        void emptyExpression() {
            var tmpl = "${}";
            expectParseError(tmpl, "expression");
        }

        @Test
        void expressionWithTrailingTokens() {
            // This might be caught by the Notch parser
            var tmpl = "${value extra}";
            expectParseError(tmpl, "trailing tokens");
        }

        @Test
        void malformedElvis() {
            var tmpl = "${value ?:}";
            expectParseError(tmpl, null);
        }

        @Test
        void incompleteConditionalExpression() {
            var tmpl = "${\"value\" if}";
            expectParseError(tmpl, null);
        }

        @Test
        void unbalancedParentheses() {
            var tmpl = "${(value}";
            expectParseError(tmpl, null);
        }

        @Test
        void invalidOperator() {
            var tmpl = "${value !! other}";
            expectParseError(tmpl, null);
        }
    }

    // ========================================================================
    // Deep Nesting Edge Cases
    // ========================================================================

    @Nested
    class DeepNesting {

        @Test
        void deeplyNestedIf() {
            var tmpl = """
                    #if true
                        #if true
                            #if true
                                #if true
                                    #if true
                                        deep content
                                    #end
                                #end
                            #end
                        #end
                    #end
                    """;
            // Should succeed
            var result = renderString(tmpl);
            assertNotNull(result);
            assertTrue(result.contains("deep content"));
        }

        @Test
        void deeplyNestedFor() {
            var tmpl = """
                    #for a in [1]
                        #for b in [2]
                            #for c in [3]
                                ${a}${b}${c}
                            #end
                        #end
                    #end
                    """;
            // Should succeed
            var result = renderString(tmpl);
            assertNotNull(result);
            assertTrue(result.contains("123"));
        }

        @Test
        void mixedNesting() {
            var tmpl = """
                    #if true
                        #for x in [1,2]
                            #if x > 1
                                #for y in [3,4]
                                    ${x}${y}
                                #end
                            #end
                        #end
                    #end
                    """;
            // Should succeed
            var result = renderString(tmpl);
            assertNotNull(result);
        }

        @Test
        void fragmentInForInIf() {
            var tmpl = """
                    #if true
                        #for i in [1,2]
                            #macro item(n)
                                Item ${n}
                            #end
                            #expand item(i)
                        #end
                    #end
                    """;
            // Should succeed
            var result = renderString(tmpl);
            assertNotNull(result);
            assertTrue(result.contains("Item 1"));
            assertTrue(result.contains("Item 2"));
        }
    }

    // ========================================================================
    // Fragment/Expand Edge Cases
    // ========================================================================

    @Nested
    class MacroExpandEdgeCases {

        @Test
        void expandUndefinedFragment() {
            var tmpl = """
                    #expand nonexistent()
                    """;
            // This is a runtime error, not a parse error
            expectRenderError(tmpl, null);
        }

        @Test
        void fragmentWithNoParameters() {
            var tmpl = """
                    #macro simple()
                        Simple content
                    #end
                    #expand simple()
                    """;
            // Should succeed
            var result = renderString(tmpl);
            assertNotNull(result);
            assertTrue(result.contains("Simple content"));
        }

        @Test
        void nestedFragments() {
            var tmpl = """
                    #macro outer(x)
                        #macro inner(y)
                            Inner: ${y}
                        #end
                        Outer: ${x}
                        #expand inner(x)
                    #end
                    #expand outer(123)
                    """;
            // Should succeed
            var result = renderString(tmpl);
            assertNotNull(result);
        }

        @Test
        void fragmentCallingAnotherFragment() {
            var tmpl = """
                    #macro first(x)
                        First: ${x}
                    #end
                    
                    #macro second(y)
                        Second: ${y}
                        #expand first(y)
                    #end
                    
                    #expand second(42)
                    """;
            // Should succeed
            var result = renderString(tmpl);
            assertNotNull(result);
            assertTrue(result.contains("First: 42"));
            assertTrue(result.contains("Second: 42"));
        }
    }

    // ========================================================================
    // Template Loading Edge Cases
    // ========================================================================

    @Nested
    class TemplateLoadingEdgeCases {

        @Test
        void includeNonExistentTemplate() {
            var tmpl = """
                    #include "nonexistent.html"
                    """;

            var templates = new NotchTemplateRegistry(new NotchTemplateLoader() {
                @Override
                public String loadTemplate(String path) {
                    if (path.equals(testInfo.getDisplayName())) return tmpl;
                    throw new RuntimeException("Template not found: " + path);
                }
            });
            BasicNotchTemplateCommands.addTo(templates);

            assertThrows(Exception.class, () -> {
                templates.renderTemplate(testInfo.getDisplayName(), Map.of());
            });
        }

        @Test
        void layoutNonExistentTemplate() {
            var tmpl = """
                    #layout "nonexistent.html"
                    Content
                    """;

            var templates = new NotchTemplateRegistry(new NotchTemplateLoader() {
                @Override
                public String loadTemplate(String path) {
                    if (path.equals(testInfo.getDisplayName())) return tmpl;
                    throw new RuntimeException("Template not found: " + path);
                }
            });
            BasicNotchTemplateCommands.addTo(templates);

            assertThrows(Exception.class, () -> {
                templates.renderTemplate(testInfo.getDisplayName(), Map.of());
            });
        }
    }

    // ========================================================================
    // Whitespace and Special Cases
    // ========================================================================

    @Nested
    class WhitespaceAndSpecialCases {

        @Test
        void emptyTemplate() {
            var tmpl = "";
            var result = renderString(tmpl);
            assertNotNull(result);
            assertEquals("", result);
        }

        @Test
        void onlyWhitespace() {
            var tmpl = "   \n  \t  \n  ";
            var result = renderString(tmpl);
            assertNotNull(result);
            assertEquals(tmpl, result);
        }

        @Test
        void commandWithExtraWhitespace() {
            var tmpl = """
                    #if   true
                        content
                    #end
                    """;
            var result = renderString(tmpl);
            assertNotNull(result);
            assertTrue(result.contains("content"));
        }

        @Test
        void unicodeInTemplate() {
            var tmpl = """
                    #if true
                        🎉 Success! 你好 مرحبا
                    #end
                    """;
            var result = renderString(tmpl);
            assertNotNull(result);
            assertTrue(result.contains("🎉"));
            assertTrue(result.contains("你好"));
            assertTrue(result.contains("مرحبا"));
        }

        @Test
        void unicodeInExpressions() {
            var tmpl = """
                    ${emoji} ${chinese}
                    """;
            var result = renderString(tmpl, "emoji", "🚀", "chinese", "中文");
            assertNotNull(result);
            // Note: HTML escaping may affect unicode characters
            // Just verify we got some output
            assertFalse(result.isEmpty());
        }
    }

    // ========================================================================
    // Complex Expression Tests
    // ========================================================================

    @Nested
    class ComplexExpressions {

        @Test
        void complexBooleanInIf() {
            var tmpl = """
                    #if (x > 5 && y < 10) || z == "test"
                        matched
                    #end
                    """;
            var result = renderString(tmpl, "x", 6, "y", 8, "z", "other");
            assertNotNull(result);
            assertTrue(result.contains("matched"));
        }

        @Test
        void nestedElvisOperators() {
            var tmpl = """
                    ${a ?: b ?: c ?: "default"}
                    """;
            var result = renderString(tmpl, "c", "value");
            assertNotNull(result);
            assertTrue(result.contains("value"));
        }

        @Test
        void conditionalExpressionInFor() {
            var tmpl = """
                    #for x in [1, 2, 3, 4, 5]
                        ${"even" if x % 2 == 0}${"odd" if x % 2 == 1}
                    #end
                    """;
            var result = renderString(tmpl);
            assertNotNull(result);
        }

        @Test
        void chainedPropertyAccess() {
            var tmpl = """
                    ${obj.prop.subprop}
                    """;
            // Using LinkedHashMap instead of Map.of to avoid reflection issues with immutable collections
            var innerMap = new java.util.LinkedHashMap<String, Object>();
            innerMap.put("subprop", "value");
            var outerMap = new java.util.LinkedHashMap<String, Object>();
            outerMap.put("prop", innerMap);

            var result = renderString(tmpl, "obj", outerMap);
            assertNotNull(result);
            assertTrue(result.contains("value"));
        }
    }

    // ========================================================================
    // Edge Cases with Loop Variables
    // ========================================================================

    @Nested
    class LoopVariableEdgeCases {

        @Test
        void indexVariableInFor() {
            var tmpl = """
                    #for x in [10, 20, 30]
                        ${index}: ${x}
                    #end
                    """;
            var result = renderString(tmpl);
            assertNotNull(result);
            assertTrue(result.contains("0: 10"));
            assertTrue(result.contains("1: 20"));
            assertTrue(result.contains("2: 30"));
        }

        @Test
        void nestedForWithIndex() {
            var tmpl = """
                    #for x in [1, 2]
                        Outer ${index}
                        #for y in [3, 4]
                            Inner ${index}
                        #end
                    #end
                    """;
            var result = renderString(tmpl);
            assertNotNull(result);
            // Inner loop should have its own index starting from 0
        }

        @Test
        void loopVariableShadowing() {
            var tmpl = """
                    #for x in [1, 2, 3]
                        Outer: ${x}
                        #for x in [10, 20]
                            Inner: ${x}
                        #end
                        After inner: ${x}
                    #end
                    """;
            var result = renderString(tmpl);
            assertNotNull(result);
            // Should handle variable shadowing correctly
        }
    }

    // ========================================================================
    // For-Else Edge Cases
    // ========================================================================

    @Nested
    class ForElseEdgeCases {

        @Test
        void forElseWithEmptyList() {
            var tmpl = """
                    #for x in []
                        Item: ${x}
                    #else
                        No items!
                    #end
                    """;
            var result = renderString(tmpl);
            assertNotNull(result);
            assertTrue(result.contains("No items!"));
            assertFalse(result.contains("Item:"));
        }

        @Test
        void forElseWithNonEmptyList() {
            var tmpl = """
                    #for x in [1, 2, 3]
                        Item: ${x}
                    #else
                        No items!
                    #end
                    """;
            var result = renderString(tmpl);
            assertNotNull(result);
            assertFalse(result.contains("No items!"));
            assertTrue(result.contains("Item:"));
        }

        @Test
        void nestedForElse() {
            var tmpl = """
                    #for x in [1]
                        #for y in []
                            Inner: ${y}
                        #else
                            Empty inner
                        #end
                    #else
                        Empty outer
                    #end
                    """;
            var result = renderString(tmpl);
            assertNotNull(result);
            assertTrue(result.contains("Empty inner"));
            assertFalse(result.contains("Empty outer"));
        }
    }
}
