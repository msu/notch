package edu.montana.notch.parser;

import edu.montana.notch.chisel.Diagnostic;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.montana.notch.NotchTestUtils.exec;
import static edu.montana.notch.NotchTestUtils.execDiagnostics;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NotchExpressionErrorRecoveryTest {

    private static List<String> errorsIn(String source) {
        return execDiagnostics(source).stream()
                .map(NotchExpressionErrorRecoveryTest::key)
                .toList();
    }

    private static String key(Diagnostic diagnostic) {
        if (diagnostic.getCode() != null) return diagnostic.getCode().id();
        return diagnostic.getNotes().isEmpty() ? "(no code, no note)" : diagnostic.getNotes().getFirst();
    }

    @Test
    void missingForLoopIterableDoesNotCascade() {
        assertEquals(List.of("expected expression for the loop iterable"), errorsIn("""
                for x in
                end
                """));
    }

    @Test
    void missingRepeatWhileConditionDoesNotCascade() {
        assertEquals(List.of("expected condition after 'while'"), errorsIn("""
                repeat while
                end
                """));
    }

    @Test
    void missingRepeatUntilConditionDoesNotCascade() {
        assertEquals(List.of("expected condition after 'until'"), errorsIn("""
                repeat until
                end
                """));
    }

    @Test
    void forBodyParsedAfterMissingIterable() {
        assertEquals(List.of("expected expression for the loop iterable"), errorsIn("""
                for x in
                  for y in [1, 2]
                  end
                end
                """));
    }

    @Test
    void missingRepeatCountDoesNotCascade() {
        assertEquals(List.of("expected count expression after 'repeat'"), errorsIn("""
                repeat times
                end
                """));
    }

    @Test
    void throwOnDifferentLineDoesNotCascade() {
        assertEquals(List.of("EP0014"), errorsIn("""
                throw
                print(1)
                """));
    }

    @Test
    void throwWithFailingExpressionDoesNotCascade() {
        assertEquals(List.of("EP0003"), errorsIn("""
                function foo()
                  throw 1 +
                  return 1
                end
                """));
    }

    @Test
    void missingAssignmentRhsDoesNotCascade() {
        assertEquals(List.of("expected an expression after '='"), errorsIn("""
                x =
                print(1)
                """));
    }

    @Test
    void missingIndexExpressionDoesNotCascade() {
        assertEquals(List.of("An expression is required"), errorsIn("""
                x = arr[]
                print(1)
                """));
    }

    @Test
    void missingRecoverExpressionDoesNotCascade() {
        assertEquals(List.of("expected an expression after 'recover'"), errorsIn("""
                x = someVal recover with
                print(1)
                """));
    }

    @Test
    void missingFieldInitializerDoesNotCascade() {
        assertEquals(List.of("expected an initializer expression after '='"), errorsIn("""
                class Dog
                  field age =
                  field name
                end
                """));
    }

    @Test
    void missingTypedRecoverExpressionDoesNotCascade() {
        assertEquals(List.of("EP0005"), errorsIn("""
                x = someVal recover IOException with
                print(1)
                """));
    }

    @Test
    void failingCallStatementDoesNotCascade() {
        assertEquals(List.of("EP0003"), errorsIn("""
                foo(1 +
                print(1)
                """));
    }

    @Test
    void validCodeUnaffected() {
        assertEquals("5\n", exec("print(2 + 3)"));
    }

    @Test
    @Disabled("known bug: synchronize() does not stop at '}', so an error inside a closure "
            + "body consumes the closing brace and cascades into a spurious "
            + "\"Require a '}'\" error")
    void errorInClosureBodyDoesNotConsumeClosingBrace() {
        assertEquals(List.of("EP0011"), errorsIn("""
                x = \\ -> { bogus bogus }
                print(1)
                """));
    }

    @Test
    @Disabled("known bug: parseMethodInvocation adds parseExpression()'s null return to the "
            + "argument list without a diagnostic, and NotchMethodInvocation's List.copyOf "
            + "throws a raw NullPointerException that bypasses error recovery entirely")
    void missingCallArgumentDoesNotCrashParser() {
        assertFalse(errorsIn("foo(, 1)").isEmpty());
    }
}
