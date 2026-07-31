package edu.montana.notch.parser;

import edu.montana.notch.errors.ParserError;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static edu.montana.notch.NotchTestUtils.countCode;
import static edu.montana.notch.NotchTestUtils.exec;
import static edu.montana.notch.NotchTestUtils.hasCode;
import static org.junit.jupiter.api.Assertions.*;

class ParserErrorSynchronizationTest {

    @Test
    void multipleTopLevelErrorsReported() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                return 1
                return 2
                x = 1 + 1
                """));
        assertTrue(hasCode(ex, ParserError.EP0019),
                "expected a return-outside-function error, got: " + ex.getMessage());
        assertEquals(2, countCode(ex, ParserError.EP0019),
                "expected 2 return-outside-function errors, got: " + ex.getMessage());
    }

    @Test
    void errorInsideIfDoesNotCascade() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                if true
                  return 5
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "unexpected cascade error: " + ex.getMessage());
    }

    @Test
    void errorInsideForDoesNotCascade() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                for i in [1, 2, 3]
                  return 5
                end
                """));
        assertFalse(ex.getMessage().contains("Unterminated"),
                "unexpected cascade error: " + ex.getMessage());
    }

    @Test
    void errorInsideFunctionDoesNotCascade() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                function foo()
                  rethrow
                end
                """));
        assertFalse(ex.getMessage().contains("Unterminated"),
                "unexpected cascade error: " + ex.getMessage());
    }

    @Test
    void errorInsideTryDoesNotCascade() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                try
                  return 5
                catch e
                end
                """));
        assertFalse(ex.getMessage().contains("Unterminated"),
                "unexpected cascade error: " + ex.getMessage());
    }

    @Test
    void errorInNestedBlockIsCollected() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                if true
                  return 5
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "nested error should be reported, got: " + ex.getMessage());
    }

    @Test
    void nestedAndTopLevelErrorsBothReported() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                if true
                  return 5
                end
                return 10
                """));
        long count = countCode(ex, ParserError.EP0019);
        assertTrue(count >= 2,
                "expected 2 errors (nested + top-level), got: " + ex.getMessage());
    }

    @Test
    void singleErrorBehaviorUnchanged() {
        var ex = assertThrows(RuntimeException.class, () -> exec("return 5"));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected return-outside-function error, got: " + ex.getMessage());
    }

    @Test
    void validCodeUnaffected() {
        assertEquals("5\n", exec("print(2 + 3)"));
    }

    @Test
    void multiLineValidCodeUnaffected() {
        assertEquals("5\n", exec("""
                function add(a, b)
                  return a + b
                end
                print(add(2, 3))
                """));
    }

    // --- class body error recovery (inline scoped sync) ---

    @Test
    void errorInClassBodyDoesNotConsumeEnd() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                class Dog
                  field age
                  bogus
                  field name
                end
                """));
        assertTrue(ex.getMessage().contains("'field' or 'function'"),
                "expected class-body error, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "class 'end' should not be consumed during recovery: " + ex.getMessage());
    }

    @Test
    void multipleErrorsInClassBodyAllReported() {
        // Bad tokens separated by a valid 'field' force two distinct sync passes,
        // one error per pass.  Consecutive bad tokens (no valid decl between them)
        // would be consumed in a single sync pass, producing only one error.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                class Dog
                  bogus1
                  field age
                  bogus2
                  field name
                end
                """));
        long count = countCode(ex, ParserError.EP0020);
        assertTrue(count >= 2,
                "expected 2 class-body errors, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "class 'end' should not be consumed: " + ex.getMessage());
    }

    @Test
    void syncKeywordInClassBodyDoesNotCascade() {
        // 'if' is in SYNC_KEYWORDS but not in the class-body stop set {field, function, end}.
        // The inline scoped sync consumes 'if' and lands on 'field', producing exactly one error.
        // If synchronize() were called instead, it would stop at 'if' (SYNC_KEYWORD) without
        // consuming it, and the class-body loop would re-throw on 'if' endlessly.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                class Dog
                  if
                  field age
                end
                """));
        long count = countCode(ex, ParserError.EP0020);
        assertTrue(count == 1,
                "expected exactly 1 error when a sync keyword appears in class body, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "class 'end' should not be consumed: " + ex.getMessage());
    }

    // --- sync keyword coverage ---
    // Pattern: use 'return 1' (or 'rethrow') as the error source so the error message
    // is specific ("'return' outside a function"), letting us assert no "expected a statement"
    // cascade from the sync target keyword being mishandled.

    @Test
    void syncOnRepeat() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                return 1
                repeat 3 times
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "repeat block should parse cleanly after sync: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "repeat keyword should not cascade: " + ex.getMessage());
    }

    @Test
    void syncOnPrint() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                return 1
                print(42)
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "print should parse cleanly after sync: " + ex.getMessage());
    }

    @Test
    void syncOnThrow() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                return 1
                throw new RuntimeException("ok")
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "throw should parse cleanly after sync: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "throw should not appear unterminated: " + ex.getMessage());
    }

    @Test
    void syncOnReturn() {
        // 'rethrow' outside a catch is the error source; sync stops AT 'return'
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                function foo()
                  rethrow
                  return 1
                end
                """));
        assertTrue(ex.getMessage().contains("'rethrow' outside a catch"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "function should not appear unterminated: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "return should not cascade: " + ex.getMessage());
    }

    @Test
    void syncOnRethrow() {
        // 'return 1' in a catch (outside function) is the error source; sync stops AT 'rethrow'
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                try
                  x = 1
                catch e
                  return 1
                  rethrow
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "try block should not appear unterminated: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "rethrow should not cascade: " + ex.getMessage());
    }

    @Test
    void syncOnClass() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                return 1
                class Foo
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "class should parse cleanly after sync: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "class keyword should not cascade: " + ex.getMessage());
    }

    @Test
    void syncOnBreak() {
        // 'return 1' inside a loop (outside function) is the error source; sync stops AT 'break'
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                repeat 3 times
                  return 1
                  break
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "repeat block should not appear unterminated: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "break should not cascade: " + ex.getMessage());
    }

    @Test
    void syncOnContinue() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                repeat 3 times
                  return 1
                  continue
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "repeat block should not appear unterminated: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "continue should not cascade: " + ex.getMessage());
    }

    @Test
    void syncOnElse() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                if true
                  return 1
                else
                  print(1)
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "if block should not appear unterminated: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "else should not cascade: " + ex.getMessage());
    }

    // --- tokens.index == mark advance guard ---

    @Test
    @Timeout(5)
    void syncKeywordAtTopLevelIsConsumedNotLooped() {
        // 'end' at top level: every parser fails without consuming any token, so
        // tokens.index == mark in the catch block. The take() guard advances past
        // 'end' before synchronize(). Without it, synchronize() would see a
        // SYNC_KEYWORD, stop immediately, and the outer loop would call
        // parseStatement() on the same token again — infinite loop.
        var ex = assertThrows(RuntimeException.class, () -> exec("end"));
        assertTrue(ex.getMessage().contains("expected a statement"),
                "expected a parse error: " + ex.getMessage());
    }

    // --- missing sync-keyword coverage (if, for, try, function) ---

    @Test
    void syncOnIf() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                return 1
                if true
                  print(2)
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "if should not cascade: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "if block should parse cleanly after sync: " + ex.getMessage());
    }

    @Test
    void syncOnFor() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                return 1
                for x in [1, 2]
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "for should not cascade: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "for block should parse cleanly after sync: " + ex.getMessage());
    }

    @Test
    void syncOnTry() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                return 1
                try
                  x = 1
                catch e
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "try should not cascade: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "try block should parse cleanly after sync: " + ex.getMessage());
    }

    @Test
    void syncOnFunction() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                return 1
                function foo()
                  print(1)
                end
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "function keyword should not cascade: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "function block should parse cleanly after sync: " + ex.getMessage());
    }

    // --- break/continue outside loop ---

    @Test
    void breakOutsideLoopDoesNotCascade() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                break
                print(1)
                """));
        assertTrue(ex.getMessage().contains("'break' outside a loop"),
                "expected break-outside-loop error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "print should parse cleanly after sync: " + ex.getMessage());
    }

    @Test
    void continueOutsideLoopDoesNotCascade() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                continue
                print(1)
                """));
        assertTrue(ex.getMessage().contains("'continue' outside a loop"),
                "expected continue-outside-loop error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "print should parse cleanly after sync: " + ex.getMessage());
    }

    // --- synchronize() scans to EOF ---

    @Test
    void syncToEofWithUnrecognizedTokens() {
        // After 'return 1' error, synchronize() scans bogus1 and bogus2 (not sync keywords)
        // until EOF without finding any sync token. Verifies no crash and exactly one error.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                return 1
                bogus1 bogus2
                """));
        assertTrue(ex.getMessage().contains("'return' outside a function"),
                "expected the real error: " + ex.getMessage());
        long count = countCode(ex, ParserError.EP0019);
        assertTrue(count == 1,
                "expected exactly one error, got: " + ex.getMessage());
    }

    // --- missing if condition (potential cascade bug) ---

    @Test
    @Disabled("known bug: synchronize() stops AT 'end' but the enclosing if was abandoned, "
            + "so no block parser is left to consume it — the top-level loop re-parses 'end' "
            + "and cascades 'expected a statement'")
    void missingIfConditionDoesNotCascade() {
        // parseIfStatement consumes 'if' then throws (condition missing).
        // advancedFrom(mark) is true so no forced take(); synchronize() stops AT 'end'
        // (SYNC_KEYWORD) without consuming it. The outer loop should not re-parse
        // 'end' as "expected a statement".
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                if
                end
                """));
        assertTrue(ex.getMessage().contains("expected a conditional expression after 'if'"),
                "expected the real error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "end should not be re-parsed as unexpected: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "should not appear unterminated: " + ex.getMessage());
    }

    // --- method body error inside class ---

    @Test
    void methodBodyErrorInsideClassDoesNotCascade() {
        // A statement error inside a method body is recovered by parseStatement()'s own
        // catch block; it never propagates to the class-body catch and does not consume
        // the function's or class's 'end'.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                class Dog
                  function foo()
                    bogus
                  end
                  field age
                end
                """));
        assertTrue(ex.getMessage().contains("this expression cannot be used as a statement"),
                "expected body parse error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "neither function nor class should appear unterminated: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("'field' or 'function'"),
                "error should not cascade to class-body level: " + ex.getMessage());
        long count = countCode(ex, ParserError.EP0013);
        assertTrue(count == 1,
                "expected exactly one error, got: " + ex.getMessage());
    }

    // --- mark-based conditional advance fix ---

    @Test
    void validStatementAfterFailedStatementIsNotDropped() {
        // parseFunctionDeclaration() consumes 'function' then fails (no name follows).
        // tokens.index > mark, so the catch does not advance again; synchronize() stops
        // immediately at the second 'function' (SYNC_KEYWORD).  Without the mark check the
        // old advance-first would eat 'function foo() ... end', leaving 'return 1' at the
        // top level and causing a spurious "'return' outside a function" error.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                function
                function foo()
                  return 1
                end
                """));
        assertFalse(ex.getMessage().contains("'return' outside a function"),
                "return inside second function should not appear as a top-level error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "second function block should parse cleanly: " + ex.getMessage());
    }

    // --- sync stops at 'catch' ---

    @Test
    void syncOnCatch() {
        // 'return 1' inside the try body is the error source; synchronize() stops AT
        // 'catch' (SYNC_KEYWORD) without consuming it, so the catch clause still parses
        // and the try terminates cleanly with exactly one error.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                try
                  return 1
                catch e
                  x = 1
                end
                """));
        long count = countCode(ex, ParserError.EP0019);
        assertTrue(count == 1,
                "expected exactly one error, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "try block should not appear unterminated: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "catch should not cascade: " + ex.getMessage());
    }

    // --- multiple errors inside a single non-class block ---

    @Test
    void multipleErrorsInFunctionBodyAllReported() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                function foo()
                  rethrow
                  rethrow
                end
                """));
        long count = countCode(ex, ParserError.EP0015);
        assertTrue(count == 2,
                "expected exactly 2 errors in the function body, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "function should not appear unterminated: " + ex.getMessage());
    }

    @Test
    void multipleErrorsInLoopBodyAllReported() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                repeat 2 times
                  return 1
                  return 2
                end
                """));
        long count = countCode(ex, ParserError.EP0019);
        assertTrue(count == 2,
                "expected exactly 2 errors in the loop body, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "repeat block should not appear unterminated: " + ex.getMessage());
    }

    // --- exact error counts (guards against duplicate diagnostics) ---

    @Test
    void topLevelErrorCountIsExact() {
        // Tightens multipleTopLevelErrorsReported's `>= 2`: each bad statement must
        // produce exactly one diagnostic, so a duplicate-diagnostic regression fails here.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                return 1
                return 2
                x = 1 + 1
                """));
        long count = countCode(ex, ParserError.EP0019);
        assertTrue(count == 2,
                "expected exactly 2 errors, got: " + ex.getMessage());
    }

    // --- class-body sync vs nested blocks ---

    @Test
    @Disabled("known limitation: the inline class-body sync stops at the nested if's 'end', "
            + "prematurely closing the class and cascading 'expected a statement' errors")
    void nestedBlockGarbageInClassBodyDoesNotCascade() {
        // The scoped sync consumes until {field, function, end} — it cannot tell the
        // nested if's 'end' from the class's own 'end', so 'field age' and the real
        // class 'end' leak to the top level.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                class Dog
                  if true
                    print(1)
                  end
                  field age
                end
                """));
        long count = countCode(ex, ParserError.EP0020);
        assertTrue(count == 1,
                "expected exactly one class-body error, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "class members should not leak to the top level: " + ex.getMessage());
    }
}
