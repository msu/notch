package edu.montana.notch.parser;

import edu.montana.notch.errors.ParserError;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static edu.montana.notch.NotchTestUtils.assertCodes;
import static edu.montana.notch.NotchTestUtils.execDiagnostics;

class ParserErrorSynchronizationTest {

    @Test
    void multipleTopLevelErrorsReported() {
        var diagnostics = execDiagnostics("""
                return 1
                return 2
                x = 1 + 1
                """);
        assertCodes(diagnostics, ParserError.EP0019, ParserError.EP0019);
    }

    @Test
    void errorInsideIfDoesNotCascade() {
        var diagnostics = execDiagnostics("""
                if true
                  return 5
                end
                """);
        assertCodes(diagnostics, ParserError.EP0019);
    }

    @Test
    void errorInsideForDoesNotCascade() {
        var diagnostics = execDiagnostics("""
                for i in [1, 2, 3]
                  return 5
                end
                """);
        assertCodes(diagnostics, ParserError.EP0019);
    }

    @Test
    void errorInsideFunctionDoesNotCascade() {
        var diagnostics = execDiagnostics("""
                function foo()
                  rethrow
                end
                """);
        assertCodes(diagnostics, ParserError.EP0015);
    }

    @Test
    void errorInsideTryDoesNotCascade() {
        var diagnostics = execDiagnostics("""
                try
                  return 5
                catch e
                end
                """);
        assertCodes(diagnostics, ParserError.EP0019);
    }

    @Test
    void nestedAndTopLevelErrorsBothReported() {
        var diagnostics = execDiagnostics("""
                if true
                  return 5
                end
                return 10
                """);
        assertCodes(diagnostics, ParserError.EP0019, ParserError.EP0019);
    }

    @Test
    void errorInClassBodyDoesNotConsumeEnd() {
        var diagnostics = execDiagnostics("""
                class Dog
                  field age
                  bogus
                  field name
                end
                """);
        assertCodes(diagnostics, ParserError.EP0020);
    }

    @Test
    void multipleErrorsInClassBodyAllReported() {
        var diagnostics = execDiagnostics("""
                class Dog
                  bogus1
                  field age
                  bogus2
                  field name
                end
                """);
        assertCodes(diagnostics, ParserError.EP0020, ParserError.EP0020);
    }

    @Test
    void syncKeywordInClassBodyDoesNotCascade() {
        var diagnostics = execDiagnostics("""
                class Dog
                  if
                  field age
                end
                """);
        assertCodes(diagnostics, ParserError.EP0020);
    }

    @Test
    void syncOnRepeat() {
        var diagnostics = execDiagnostics("""
                return 1
                repeat 3 times
                end
                """);
        assertCodes(diagnostics, ParserError.EP0019);
    }

    @Test
    void syncOnPrint() {
        var diagnostics = execDiagnostics("""
                x + 1 = 5
                print(1 +
                """);
        assertCodes(diagnostics, ParserError.EP0012, ParserError.EP0003);
    }

    @Test
    void syncOnThrow() {
        var diagnostics = execDiagnostics("""
                x + 1 = 5
                throw
                """);
        assertCodes(diagnostics, ParserError.EP0012, ParserError.EP0014);
    }

    @Test
    void syncOnReturn() {
        var diagnostics = execDiagnostics("""
                x + 1 = 5
                return 1
                """);
        assertCodes(diagnostics, ParserError.EP0012, ParserError.EP0019);
    }

    @Test
    void syncOnRethrow() {
        var diagnostics = execDiagnostics("""
                x + 1 = 5
                rethrow
                """);
        assertCodes(diagnostics, ParserError.EP0012, ParserError.EP0015);
    }

    @Test
    void syncOnClass() {
        var diagnostics = execDiagnostics("""
                return 1
                class Foo
                end
                """);
        assertCodes(diagnostics, ParserError.EP0019);
    }

    @Test
    void syncOnBreak() {
        var diagnostics = execDiagnostics("""
                x + 1 = 5
                break
                """);
        assertCodes(diagnostics, ParserError.EP0012, ParserError.EP0018);
    }

    @Test
    void syncOnContinue() {
        var diagnostics = execDiagnostics("""
                x + 1 = 5
                continue
                """);
        assertCodes(diagnostics, ParserError.EP0012, ParserError.EP0018);
    }

    @Test
    void syncOnElse() {
        var diagnostics = execDiagnostics("""
                x + 1 = 5
                else
                """);
        assertCodes(diagnostics, ParserError.EP0012, ParserError.EP0062);
    }

    @Test
    void syncOnCatch() {
        var diagnostics = execDiagnostics("""
                x + 1 = 5
                catch e
                """);
        assertCodes(diagnostics, ParserError.EP0012, ParserError.EP0061);
    }

    @Test
    void syncOnIf() {
        var diagnostics = execDiagnostics("""
                return 1
                if true
                  print(2)
                end
                """);
        assertCodes(diagnostics, ParserError.EP0019);
    }

    @Test
    void syncOnFor() {
        var diagnostics = execDiagnostics("""
                return 1
                for x in [1, 2]
                end
                """);
        assertCodes(diagnostics, ParserError.EP0019);
    }

    @Test
    void syncOnTry() {
        var diagnostics = execDiagnostics("""
                return 1
                try
                  x = 1
                catch e
                end
                """);
        assertCodes(diagnostics, ParserError.EP0019);
    }

    @Test
    void syncOnFunction() {
        var diagnostics = execDiagnostics("""
                return 1
                function foo()
                  print(1)
                end
                """);
        assertCodes(diagnostics, ParserError.EP0019);
    }

    @Test
    @Timeout(5)
    void syncKeywordAtTopLevelIsConsumedNotLooped() {
        var diagnostics = execDiagnostics("end");
        assertCodes(diagnostics, ParserError.EP0011);
    }

    @Test
    void breakOutsideLoopDoesNotCascade() {
        var diagnostics = execDiagnostics("""
                break
                print(1)
                """);
        assertCodes(diagnostics, ParserError.EP0018);
    }

    @Test
    void continueOutsideLoopDoesNotCascade() {
        var diagnostics = execDiagnostics("""
                continue
                print(1)
                """);
        assertCodes(diagnostics, ParserError.EP0018);
    }

    @Test
    void syncToEofWithUnrecognizedTokens() {
        var diagnostics = execDiagnostics("""
                return 1
                bogus1 bogus2
                """);
        assertCodes(diagnostics, ParserError.EP0019);
    }

    @Test
    @Disabled("known bug: synchronize() stops AT 'end' but the enclosing if was abandoned, "
            + "so no block parser is left to consume it the top-level loop re-parses 'end' "
            + "and cascades 'expected a statement'")
    void missingIfConditionDoesNotCascade() {
        var diagnostics = execDiagnostics("""
                if
                end
                """);
        assertCodes(diagnostics, ParserError.EP0001);
    }

    @Test
    void methodBodyErrorInsideClassDoesNotCascade() {
        var diagnostics = execDiagnostics("""
                class Dog
                  function foo()
                    bogus
                  end
                  field age
                end
                """);
        assertCodes(diagnostics, ParserError.EP0013);
    }

    @Test
    void validStatementAfterFailedStatementIsNotDropped() {
        var diagnostics = execDiagnostics("""
                function
                function foo()
                  return 1
                end
                """);
        assertCodes(diagnostics, ParserError.EP0040);
    }

    @Test
    void multipleErrorsInFunctionBodyAllReported() {
        var diagnostics = execDiagnostics("""
                function foo()
                  rethrow
                  rethrow
                end
                """);
        assertCodes(diagnostics, ParserError.EP0015, ParserError.EP0015);
    }

    @Test
    void multipleErrorsInLoopBodyAllReported() {
        var diagnostics = execDiagnostics("""
                repeat 2 times
                  return 1
                  return 2
                end
                """);
        assertCodes(diagnostics, ParserError.EP0019, ParserError.EP0019);
    }

    @Test
    @Disabled("known limitation: the inline class-body sync stops at the nested if's 'end', "
            + "prematurely closing the class and cascading 'expected a statement' errors")
    void nestedBlockGarbageInClassBodyDoesNotCascade() {
        var diagnostics = execDiagnostics("""
                class Dog
                  if true
                    print(1)
                  end
                  field age
                end
                """);
        assertCodes(diagnostics, ParserError.EP0020);
    }
}
