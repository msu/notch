package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotchRecoverExprTest {

    private static final String BOOM = """
            function boom()
                throw "kaboom"
            end
            """;

    private static final String IO_ERROR = """
            function ioError()
                throw IOException("disk gone")
            end
            """;

    @Test
    void recoverProvidesValueOnError() {
        assertEquals("99\n", exec(BOOM + """
                x = boom() recover 99
                print(x)
                """));
    }

    @Test
    void recoverNotUsedOnSuccess() {
        assertEquals("7\n", exec("""
                function ok()
                    return 7
                end
                x = ok() recover 99
                print(x)
                """));
    }

    @Test
    void recoverWithWorks() {
        assertEquals("99\n", exec(BOOM + """
                x = boom() recover with 99
                print(x)
                """));
    }

    @Test
    void catchAfterExprGivesClearError() {
        var ex = assertThrows(RuntimeException.class, () -> exec(BOOM + """
                x = boom() catch print("logged") recover 0
                """));
        assertTrue(ex.getMessage().contains("try/catch block"),
                "expected guidance about try/catch block but got: " + ex.getMessage());
    }

    @Test
    void typedRecoverMatchesType() {
        assertEquals("1\n", exec(IO_ERROR + """
                x = ioError() recover from IOException 1 recover 0
                print(x)
                """));
    }

    @Test
    void typedRecoverMatchesTypeWithoutFrom() {
        assertEquals("1\n", exec(IO_ERROR + """
                x = ioError() recover IOException 1 recover 0
                print(x)
                """));
    }

    @Test
    void typedRecoverFallsToUntyped() {
        assertEquals("0\n", exec(BOOM + """
                x = boom() recover from IOException 1 recover 0
                print(x)
                """));
    }

    @Test
    void typedRecoverFallsToUntypedWithoutFrom() {
        assertEquals("0\n", exec(BOOM + """
                x = boom() recover IOException 1 recover 0
                print(x)
                """));
    }

    @Test
    void typedRecoverFirstMatch() {
        assertEquals("1\n", exec(IO_ERROR + """
                x = ioError() recover from IOException 1 recover from Exception 2 recover 0
                print(x)
                """));
    }

    @Test
    void typedRecoverFirstMatchWithoutFrom() {
        assertEquals("1\n", exec(IO_ERROR + """
                x = ioError() recover IOException 1 recover Exception 2 recover 0
                print(x)
                """));
    }

    @Test
    void typedRecoverRethrowsOnNoMatch() {
        var ex = assertThrows(RuntimeException.class, () -> exec(BOOM + """
                x = boom() recover from IOException 1
                print(x)
                """));
        assertTrue(ex.getMessage().contains("kaboom"),
                "expected rethrown exception message, got: " + ex.getMessage());
    }

    @Test
    void typedRecoverUnknownTypeFails() {
        var ex = assertThrows(RuntimeException.class, () -> exec(BOOM + """
                x = boom() recover from FakeException 1 recover 0
                print(x)
                """));
        assertTrue(ex.getMessage().contains("unknown exception type 'FakeException'"),
                "expected unknown-type error, got: " + ex.getMessage());
    }

    @Test
    void recoverFromWithWorks() {
        assertEquals("1\n", exec(IO_ERROR + """
                x = ioError() recover from IOException with 1 recover 0
                print(x)
                """));
    }

    @Test
    void recoverTypeWithWithoutFrom() {
        assertEquals("1\n", exec(IO_ERROR + """
                x = ioError() recover IOException with 1 recover 0
                print(x)
                """));
    }

    @Test
    void typedRecoverOnContinuationLine() {
        assertEquals("1\n", exec(IO_ERROR + """
                x = ioError() recover from IOException 1
                              recover 0
                print(x)
                """));
    }

    @Test
    void multipleTypedRecoversContinuationLines() {
        assertEquals("1\n", exec(IO_ERROR + """
                x = ioError() recover from IOException 1
                              recover from RuntimeException 2
                              recover 0
                print(x)
                """));
    }

    @Test
    void recoverContinuationInsideTry() {
        assertEquals("1\n", exec(IO_ERROR + """
                try
                    x = ioError() recover from IOException 1
                                  recover 0
                catch RuntimeException
                    x = 99
                end
                print(x)
                """));
    }

    @Test
    void untypedRecoverTreatedAsExpr() {
        assertEquals("7\n", exec("""
                function fallback()
                    return 7
                end
                function boom()
                    throw "kaboom"
                end
                x = boom() recover fallback()
                print(x)
                """));
    }

    @Test
    void typedRecoverExpressionFallback() {
        assertEquals("7\n", exec(IO_ERROR + """
                function fallback()
                    return 7
                end
                x = ioError() recover IOException with fallback()
                print(x)
                """));
    }

    @Test
    void strayTokenAfterTypedRecoverGivesError() {
        var ex = assertThrows(RuntimeException.class, () -> exec(IO_ERROR + """
                x = ioError() recover IOException 1 garbage
                """));
        assertTrue(ex.getMessage().contains("unexpected token after recover"),
                "expected stray token error but got: " + ex.getMessage());
    }

    @Test
    void typedRecoverMissingFallbackGivesError() {
        var ex = assertThrows(RuntimeException.class, () -> exec(IO_ERROR + """
                x = ioError() recover IOException recover 0
                """));
        assertTrue(ex.getMessage().contains("expected expression after recover type"),
                "expected missing fallback error but got: " + ex.getMessage());
    }
}
