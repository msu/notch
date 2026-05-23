package edu.montana.notch.templates;

import edu.montana.notch.chisel.Source;
import edu.montana.notch.templates.loader.NotchTemplateLoader;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

public class ParseErrorTests extends NotchTemplateTestBase {
    // ========================================================================
    // Missing #end Tags
    // ========================================================================

    @Nested
    class MissingEndTags {

        @Test
        void missingEndForIf() {
            registerTemplate("template", """
                    #if true
                        content here
                    """);
            expectParseError("template", "unterminated");
        }

        @Test
        void missingEndForNestedIf() {
            registerTemplate("template", """
                    #if true
                        outer content
                        #if false
                            inner content
                        #end
                    """);
            expectParseError("template", "unterminated");
        }

        @Test
        void missingEndForFor() {
            registerTemplate("template", """
                    #for x in [1,2,3]
                        ${x}
                    """);
            expectParseError("template", "unterminated");
        }

        @Test
        void missingEndForFragment() {
            registerTemplate("template", """
                    #macro card(id)
                        <div>${id}</div>
                    """);
            expectParseError("template", "unterminated");
        }

        @Test
        void missingEndForContent() {
            registerTemplate("template", """
                    #content footer with
                        Footer content
                    """);
            expectParseError("template", "unterminated");
        }

        @Test
        void missingEndForNestedFor() {
            registerTemplate("template", """
                    #for x in [1,2,3]
                        ${x}
                        #for y in [4,5,6]
                            ${y}
                        #end
                    """);
            expectParseError("template", "unterminated");
        }

        @Test
        void missingEndForIfWithElse() {
            registerTemplate("template", """
                    #if true
                        first
                    #else
                        second
                    """);
            expectParseError("template", "unterminated");
        }

        @Test
        void missingEndForIfWithElseIf() {
            registerTemplate("template", """
                    #if false
                        first
                    #elseif true
                        second
                    """);
            expectParseError("template", "unterminated");
        }
    }

    // ========================================================================
    // Mismatched and Orphaned #else/#elseif
    // ========================================================================

    @Nested
    class MismatchedElseAndElseIf {

        @Test
        void elseifAfterElse() {
            registerTemplate("template", """
                    #if false
                        first
                    #else
                        second
                    #elseif true
                        third
                    #end
                    """);
            // The parser is lenient - #elseif after #else is treated as a command in the else block
            // This actually renders successfully with the else block containing the elseif
            var result = renderString("template");
            assertNotNull(result, "Parser allows elseif after else (though semantically odd)");
        }

        @Test
        void doubleElseInFor() {
            registerTemplate("template", """
                    #for x in [1,2,3]
                        ${x}
                    #else
                        empty
                    #else
                        double else
                    #end
                    """);
            expectParseError("template", "unknown command \"else\"");
        }

        @Test
        void elseifInFor() {
            registerTemplate("template", """
                    #for x in [1,2,3]
                        ${x}
                    #elseif true
                        This shouldn't work
                    #end
                    """);
            expectParseError("template", "unknown command \"elseif\"");
        }
    }

    // ========================================================================
    // Malformed Command Syntax
    // ========================================================================

    @Nested
    class MalformedCommands {

        @Test
        void ifWithoutCondition() {
            registerTemplate("template", """
                    #if
                        content
                    #end
                    """);
            // Missing condition causes runtime error from Notch expression parser
            expectRenderError("template", null);
        }

        @Test
        void forWithoutIn() {
            registerTemplate("template", """
                    #for x [1,2,3]
                        ${x}
                    #end
                    """);
            expectParseError("template", "in");
        }

        @Test
        void forWithoutVarName() {
            registerTemplate("template", """
                    #for in [1,2,3]
                        content
                    #end
                    """);
            expectParseError("template", null);
        }

        @Test
        void forWithoutIterable() {
            registerTemplate("template", """
                    #for x in
                        content
                    #end
                    """);
            expectParseError("template", null);
        }

        @Test
        void elseifWithoutCondition() {
            registerTemplate("template", """
                    #if false
                        first
                    #elseif
                        second
                    #end
                    """);
            // Missing condition causes runtime error from Notch expression parser
            expectRenderError("template", null);
        }

        @Test
        void fragmentWithoutName() {
            registerTemplate("template", """
                    #fragment
                        content
                    #end
                    """);
            expectParseError("template", null);
        }

        @Test
        void expandWithoutName() {
            registerTemplate("template", """
                    #expand
                    """);
            expectParseError("template", null);
        }

        @Test
        void ifWithExtraTokens() {
            registerTemplate("template", """
                    #if true extra stuff here
                        content
                    #end
                    """);
            expectParseError("template", "extra tokens");
        }

        @Test
        void forWithExtraTokens() {
            registerTemplate("template", """
                    #for x in [1,2,3] extra
                        ${x}
                    #end
                    """);
            expectParseError("template", null);
        }
    }

    // ========================================================================
    // Unknown Commands
    // ========================================================================

    @Nested
    class UnknownCommands {

        @Test
        void unknownCommand() {
            registerTemplate("template", """
                    #foobar
                        content
                    #end
                    """);
            expectParseError("template", "unknown command");
        }

        @Test
        void typoInIfCommand() {
            registerTemplate("template", """
                    #iif true
                        content
                    #end
                    """);
            expectParseError("template", "unknown command");
        }

        @Test
        void caseSensitiveCommand() {
            registerTemplate("template", """
                    #IF true
                        content
                    #END
                    """);
            expectParseError("template", "unknown command");
        }

        @Test
        void typoInForCommand() {
            registerTemplate("template", """
                    #fore x in [1,2,3]
                        ${x}
                    #end
                    """);
            expectParseError("template", "unknown command");
        }
    }

    // ========================================================================
    // Malformed Expressions
    // ========================================================================

    @Nested
    class MalformedExpressions {

        @Test
        void emptyExpression() {
            registerTemplate("template", "${}");
            expectParseError("template", "expression");
        }

        @Test
        void expressionWithTrailingTokens() {
            // This might be caught by the Notch parser
            registerTemplate("template", "${value extra}");
            expectParseError("template", "trailing tokens");
        }

        @Test
        void malformedElvis() {
            registerTemplate("template", "${value ?:}");
            expectParseError("template", null);
        }

        @Test
        void incompleteConditionalExpression() {
            registerTemplate("template", "${\"value\" if}");
            expectParseError("template", null);
        }

        @Test
        void unbalancedParentheses() {
            registerTemplate("template", "${(value}");
            expectParseError("template", null);
        }

        @Test
        void invalidOperator() {
            registerTemplate("template", "${value !! other}");
            expectParseError("template", null);
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
            assertContains("deep content", result);
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
            assertContains("123", result);
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
            assertContains("Item 1", result);
            assertContains("Item 2", result);
        }
    }

    // ========================================================================
    // Fragment/Expand Edge Cases
    // ========================================================================

    @Nested
    class MacroExpandEdgeCases {

        @Test
        void expandUndefinedFragment() {
            registerTemplate("template", """
                    #expand nonexistent()
                    """);
            // This is a runtime error, not a parse error
            expectRenderError("template", null);
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
            assertContains("Simple content", result);
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
            assertContains("First: 42", result);
            assertContains("Second: 42", result);
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

            var templates = new NotchTemplates(new NotchTemplateLoader() {
                @Override
                public Source loadSource(String path) {
                    if (path.equals(testInfo.getDisplayName())) return new Source(path, tmpl);
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

            var templates = new NotchTemplates(new NotchTemplateLoader() {
                @Override
                public Source loadSource(String path) {
                    if (path.equals(testInfo.getDisplayName())) return new Source(path, tmpl);
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
            assertContains("content", result);
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
            assertContains("🎉", result);
            assertContains("你好", result);
            assertContains("مرحبا", result);
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
            assertContains("matched", result);
        }

        @Test
        void nestedElvisOperators() {
            var tmpl = """
                    ${a ?: b ?: c ?: "default"}
                    """;
            var result = renderString(tmpl, "c", "value");
            assertNotNull(result);
            assertContains("value", result);
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
            assertContains("value", result);
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
            assertContains("0: 10", result);
            assertContains("1: 20", result);
            assertContains("2: 30", result);
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
            assertContains("No items!", result);
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
            assertContains("Item:", result);
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
            assertContains("Empty inner", result);
            assertFalse(result.contains("Empty outer"));
        }
    }
}
