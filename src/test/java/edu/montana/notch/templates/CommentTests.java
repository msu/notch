package edu.montana.notch.templates;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommentTests extends NotchTemplateTestBase {
    @Test
    public void commentBlockProducesNoOutput() {
        var result = renderString("""
                before
                #comment
                this is a note
                that should not render
                #end
                after
                """);
        assertEquals("""
                before
                after
                """, result);
    }

    @Test
    public void commentCanContainExpressionsWithoutEvaluating() {
        var result = renderString("""
                before
                #comment
                ${unknown_variable}
                #end
                after
                """);
        assertEquals("""
                before
                after
                """, result);
    }

    @Test
    public void emptyCommentBlock() {
        var result = renderString("""
                before
                #comment
                #end
                after
                """);
        assertEquals("""
                before
                after
                """, result);
    }
}
