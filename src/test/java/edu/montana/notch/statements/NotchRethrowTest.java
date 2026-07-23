package edu.montana.notch.statements;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotchRethrowTest {

    @Test
    void rethrowInBlockCatchPropagatesToOuter() {
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
    void rethrowInInlineCatchPropagates() {
        assertThrows(RuntimeException.class, () -> exec("""
                function boom()
                    throw "x"
                end
                y = boom() catch print("logged") then rethrow
                """));
    }

    @Test
    void rethrowOutsideCatchIsParseError() {
        assertThrows(RuntimeException.class, () -> exec("""
                rethrow
                """));
    }

    @Test
    void rethrowOuterTypedCatch() {
        assertEquals("io again\n", exec("""
                try
                    try
                        throw IOException("e")
                    catch IOException
                        rethrow
                    end
                catch IOException
                    print("io again")
                end
                """));
    }
}
