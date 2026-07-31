package edu.montana.notch.parser;

import edu.montana.notch.errors.ParserError;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static edu.montana.notch.NotchTestUtils.countCode;
import static org.junit.jupiter.api.Assertions.*;

class NotchExpressionErrorRecoveryTest {

    @Test
    void missingForLoopIterableDoesNotCascade() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                for x in
                end
                """));
        assertTrue(ex.getMessage().contains("expected expression for the loop iterable"),
                "expected iterable error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "end should not leak to top level: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "for loop should not appear unterminated: " + ex.getMessage());
    }

    @Test
    void missingRepeatWhileConditionDoesNotCascade() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                repeat while
                end
                """));
        assertTrue(ex.getMessage().contains("expected condition after 'while'"),
                "expected while condition error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "end should not leak to top level: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "repeat should not appear unterminated: " + ex.getMessage());
    }

    @Test
    void missingRepeatUntilConditionDoesNotCascade() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                repeat until
                end
                """));
        assertTrue(ex.getMessage().contains("expected condition after 'until'"),
                "expected until condition error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "end should not leak to top level: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "repeat should not appear unterminated: " + ex.getMessage());
    }

    @Test
    void forBodyParsedAfterMissingIterable() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                for x in
                  for y in [1, 2]
                  end
                end
                """));
        assertTrue(ex.getMessage().contains("expected expression for the loop iterable"),
                "expected iterable error: " + ex.getMessage());
        long count = ex.getMessage().lines()
                .filter(l -> l.contains("expected expression for the loop iterable"))
                .count();
        assertTrue(count == 1,
                "expected exactly one error, got: " + ex.getMessage());
    }

    // --- requireExpression call site coverage ---

    @Test
    void missingRepeatCountDoesNotCascade() {
        // requireExpression("expected count expression after 'repeat'") — null-return path.
        // 'times' is the next keyword so parseExpression() returns null; requireExpression
        // absorbs the error. takeKeyword("times") then succeeds, body and 'end' parse cleanly.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                repeat times
                end
                """));
        assertTrue(ex.getMessage().contains("expected count expression after 'repeat'"),
                "expected count error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "end should not leak to top level: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "repeat should not appear unterminated: " + ex.getMessage());
    }

    @Test
    void throwOnDifferentLineDoesNotCascade() {
        // Direct ParseException path in parseThrowStatement (not requireExpression):
        // 'throw' is on a different line from the next token, so the parser throws
        // immediately. synchronize() stops AT 'print' (SYNC_KEYWORD).
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                throw
                print(1)
                """));
        assertTrue(ex.getMessage().contains("expected an expression after 'throw' on the same line"),
                "expected throw-on-different-line error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "print should parse cleanly after sync: " + ex.getMessage());
    }

    @Test
    void throwWithFailingExpressionDoesNotCascade() {
        // requireExpression("expected an expression to throw") — ParseException catch path.
        // '1 +' fails inside the additive parser; requireExpression absorbs the exception
        // and returns a NotchErrorExpression. The throw statement is created normally and
        // 'return 1' on the next line still parses.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                function foo()
                  throw 1 +
                  return 1
                end
                """));
        assertFalse(ex.getMessage().contains("Unterminated"),
                "function should not appear unterminated: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "return should parse cleanly: " + ex.getMessage());
    }

    @Test
    void missingAssignmentRhsDoesNotCascade() {
        // requireExpression("expected an expression after '='") — null-return path.
        // After 'x = ', 'print' is a keyword and not parseable as an expression.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                x =
                print(1)
                """));
        assertTrue(ex.getMessage().contains("expected an expression after '='"),
                "expected assignment-RHS error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "print should parse cleanly: " + ex.getMessage());
    }

    @Test
    void missingIndexExpressionDoesNotCascade() {
        // requireExpression("An expression is required") in parseIndexOperation.
        // ']' is not parseable as an expression; error is absorbed inside arr[],
        // then ']' is consumed and the assignment statement completes normally.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                x = arr[]
                print(1)
                """));
        assertTrue(ex.getMessage().contains("An expression is required"),
                "expected index expression error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "print should parse cleanly: " + ex.getMessage());
    }

    @Test
    void missingRecoverExpressionDoesNotCascade() {
        // requireExpression("expected an expression after 'recover'") — null-return path.
        // After 'someVal recover with', 'print' on the next line is not parseable as
        // an expression; the error is absorbed and the recover expression is created.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                x = someVal recover with
                print(1)
                """));
        assertTrue(ex.getMessage().contains("expected an expression after 'recover'"),
                "expected recover expression error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "print should parse cleanly: " + ex.getMessage());
    }

    @Test
    void missingFieldInitializerDoesNotCascade() {
        // requireExpression("expected an initializer expression after '='") in parseFieldDeclaration.
        // 'field' on the next line is a keyword and not parseable as an expression;
        // the error is absorbed and the class body continues to parse 'field name'.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                class Dog
                  field age =
                  field name
                end
                """));
        assertTrue(ex.getMessage().contains("expected an initializer expression after '='"),
                "expected initializer error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Unterminated"),
                "class should not appear unterminated: " + ex.getMessage());
    }

    @Test
    void validCodeUnaffected() {
        assertEquals("5\n", exec("print(2 + 3)"));
    }

    // --- parseCallStatement rethrow-if-advanced branch ---

    @Test
    void failingCallStatementDoesNotCascade() {
        // parseCallStatement's parseExpression consumes 'foo(1' before failing on '+',
        // so advancedFrom(mark) is true and the ParseException is rethrown to
        // parseStatement's catch. synchronize() then stops AT 'print' (SYNC_KEYWORD).
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                foo(1 +
                print(1)
                """));
        long count = countCode(ex, ParserError.EP0003);
        assertTrue(count == 1,
                "expected exactly one error, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "print should parse cleanly after sync: " + ex.getMessage());
    }

    // --- typed recover missing expression ---

    @Test
    void missingTypedRecoverExpressionDoesNotCascade() {
        // The typed-recover branch throws directly ("expected expression after recover
        // type"); the assignment RHS's requireExpression absorbs it and the next
        // statement still parses.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                x = someVal recover IOException with
                print(1)
                """));
        assertTrue(ex.getMessage().contains("expected expression after recover type"),
                "expected typed-recover error: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("expected a statement"),
                "print should parse cleanly: " + ex.getMessage());
    }

    // --- closure body recovery ---

    @Test
    @Disabled("known bug: synchronize() does not stop at '}', so an error inside a closure "
            + "body consumes the closing brace and cascades into a spurious "
            + "\"Require a '}'\" error")
    void errorInClosureBodyDoesNotConsumeClosingBrace() {
        // 'bogus bogus' fails inside the closure body; recovery must not eat the '}'
        // or absorb the following top-level 'print(1)' into the closure.
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                x = \\ -> { bogus bogus }
                print(1)
                """));
        long count = countCode(ex, ParserError.EP0011);
        assertTrue(count == 1,
                "expected exactly one error from the closure body, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Require a '}'"),
                "closing brace should survive recovery: " + ex.getMessage());
    }

    // --- null argument in a method invocation ---

    @Test
    @Disabled("known bug: parseMethodInvocation adds parseExpression()'s null return to the "
            + "argument list without a diagnostic, and NotchMethodInvocation's List.copyOf "
            + "throws a raw NullPointerException that bypasses error recovery entirely")
    void missingCallArgumentDoesNotCrashParser() {
        var ex = assertThrows(RuntimeException.class, () -> exec("foo(, 1)"));
        assertFalse(ex instanceof NullPointerException,
                "a missing argument must surface as a parse diagnostic, not an NPE");
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("expression"),
                "expected a missing-argument diagnostic, got: " + ex.getMessage());
    }
}
