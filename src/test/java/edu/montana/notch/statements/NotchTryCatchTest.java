package edu.montana.notch.statements;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotchTryCatchTest {

    @Test
    void catchCatchesThrownValue() {
        assertEquals("caught: boom\n", exec("""
                try
                    throw "boom"
                catch
                    print("caught: " + exception)
                end
                """));
    }

    @Test
    void typedCatchCatches() {
        assertEquals("rt\n", exec("""
                try
                    throw "boom"
                catch RuntimeException
                    print("rt")
                end
                """));
    }

    @Test
    void catchBindsExplicitName() {
        assertEquals("x\n", exec("""
                try
                    throw "x"
                catch (RuntimeException e)
                    print(e)
                end
                """));
    }

    @Test
    void nonMatchingTypePropagates() {
        assertThrows(RuntimeException.class, () -> exec("""
                try
                    throw "x"
                catch IOException
                    print("io")
                end
                """));
    }

    @Test
    void firstMatchingClauseWins() {
        assertEquals("second\n", exec("""
                try
                    throw "x"
                catch IOException
                    print("first")
                catch RuntimeException
                    print("second")
                end
                """));
    }

    @Test
    void blockModeNoImplicitlyRethrow() {
        assertEquals("handled\nafter\n", exec("""
                try
                    throw "x"
                catch
                    print("handled")
                end
                print("after")
                """));
    }

    @Test
    void rethrowPropagatesToOuter() {
        assertEquals("outer: inner\n", exec("""
                try
                    try
                        throw "inner"
                    catch
                        rethrow
                    end
                catch
                    print("outer: " + exception)
                end
                """));
    }

    @Test
    void breakInsideTry() {
        assertEquals("0\n", exec("""
                x = 0
                repeat 3 times
                    try
                        break
                    catch
                        print("should not catch")
                    end
                    x = x + 1
                end
                print(x)
                """));
    }

    @Test
    void positionalNameBindingWithoutParensError() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                try
                    throw "boom"
                catch RuntimeException e
                    print(e)
                end
                """));
        assertTrue(ex.getMessage().contains("to bind the exception use 'catch (IOException e)'"),
                "expected guidance for exception name binding but got: " + ex.getMessage());
    }

}
