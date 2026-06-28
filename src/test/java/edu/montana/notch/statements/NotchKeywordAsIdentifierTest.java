package edu.montana.notch.statements;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotchKeywordAsIdentifierTest {

    @Test
    void cannotUseBreakAsLoopVariable() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                for break in [1, 2, 3]
                  print(break)
                end
                """));
        assertTrue(ex.getMessage().contains("'break' is a keyword and cannot be used as a loop variable name"),
                "expected keyword-specific error, got: " + ex.getMessage());
    }

    @Test
    void nonIdentLoopVariableGivesGenericError() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                for 5 in [1, 2, 3]
                  print(5)
                end
                """));
        assertTrue(ex.getMessage().contains("expected a variable name for the loop item"),
                "expected generic loop-variable error, got: " + ex.getMessage());
    }

    @Test
    void cannotUseRepeatAsPropertyName() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                x = 'hi'
                print(x.repeat)
                """));
        assertTrue(ex.getMessage().contains("'repeat' is a keyword and cannot be used as a property name"),
                "expected keyword-specific error, got: " + ex.getMessage());
    }

    @Test
    void cannotUsePrintAsLoopVariable() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                for print in [1, 2, 3]
                  print(print)
                end
                """));
        assertTrue(ex.getMessage().contains("'print' is a keyword and cannot be used as a loop variable name"),
                "expected keyword-specific error, got: " + ex.getMessage());
    }

    @Test
    void cannotUsePrintAsPropertyName() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                x = 'hi'
                print(x.print)
                """));
        assertTrue(ex.getMessage().contains("'print' is a keyword and cannot be used as a property name"),
                "expected keyword-specific error, got: " + ex.getMessage());
    }

    @Test
    void cannotUseClassAsPropertyName() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                x = 'hi'
                print(x.class)
                """));
        assertTrue(ex.getMessage().contains("'class' is a keyword and cannot be used as a property name"),
                "expected keyword-specific error, got: " + ex.getMessage());
    }
}
