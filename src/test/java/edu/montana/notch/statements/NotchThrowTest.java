package edu.montana.notch.statements;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotchThrowTest {

    @Test
    void throwStringCaughtAndBound() {
        assertEquals("boom\n", exec("""
                try
                    throw "boom"
                catch
                    print(exception)
                end
                """));
    }

    @Test
    void throwValueCaughtAndBound() {
        assertEquals("42\n", exec("""
                try
                    throw 42
                catch
                    print(exception)
                end
                """));
    }

    @Test
    void uncaughtThrowPropagates() {
        assertThrows(RuntimeException.class, () -> exec("""
                throw "unhandled"
                """));

    }

    @Test
    void throwJavaExceptionCaughtByExactType() {
        //Note `throw IOException(...)` builds a real java.io.IOException
        assertEquals("io\n", exec("""
                try
                    throw IOException("disk gone")
                catch IOException
                    print("io")
                end
                """));
    }

    @Test
    void throwJavaExceptionWithNewCaughtByExactType() {
        assertEquals("io\n", exec("""
                try
                    throw new IOException("disk gone")
                catch IOException
                    print("io")
                end
                """));
    }

    @Test
    void constructedExceptionMessageIsAccessible() {
        assertEquals("disk gone\n", exec("""
                try
                    throw IOException("disk gone")
                catch IOException
                    print(exception.message)
                end
                """));
    }

    @Test
    void newInstanceWorksInExpressionPosition() {
        assertEquals("hello\n", exec("""
                e = new RuntimeException("hello")
                print(e.message)
                """));
    }

    @Test
    void throwableTypeConstructibleByInvocationWithoutNew() {
        assertEquals("caught\n", exec("""
                try
                  throw RuntimeException('boom')
                catch RuntimeException as e
                  print('caught')
                end
                """));
    }

    @Test
    void catchNotchErrorAliasCatchesAnyNotchError() {
        assertEquals("caught\n", exec("""
                try
                    throw "anything"
                catch NotchError
                    print("caught")
                end
                """));
    }

    @Test
    void catchAllCatchesAnyNotchError() {
        assertEquals("caught\n", exec("""
                try
                    throw "anything"
                catch
                    print("caught")
                end
                """));
    }

    @Test
    void throwUnknownTypeGivesClearError() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> exec("""
                throw NotARealType("msg")
                """));
        String msg = ex.getMessage();
        assertTrue(msg.contains("NotARealType"),
                "expected error message to mention 'NotARealType' but was: " + msg);
        assertTrue(msg.contains("RuntimeException") && msg.contains("user-defined"),
                "expected error message to include throwable guidance but was: " + msg);
    }

    @Test
    void javaExceptionBadConstructorArgsGivesClearError() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> exec("""
                throw IOException(42, 99)
                """));
        String msg = ex.getMessage();
        assertTrue(msg.contains("IOException") && msg.contains("constructor"),
                "expected error to mention 'IOException' and 'constructor' but was: " + msg);
    }
}
