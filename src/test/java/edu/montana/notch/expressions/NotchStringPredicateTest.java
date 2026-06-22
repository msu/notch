package edu.montana.notch.expressions;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchStringPredicateTest {

    @Test
    void startsWithBasic() {
        assertEquals(true, eval("'hello world' starts with 'hello'"));
        assertEquals(false, eval("'hello world' starts with 'world'"));
    }

    @Test
    void endsWithBasic() {
        assertEquals(true, eval("'hello world' ends with 'world'"));
        assertEquals(false, eval("'hello world' ends with 'hello'"));
    }

    @Test
    void startsWithEmptyPrefix() {
        assertEquals(true, eval("'anything' starts with ''"));
    }

    @Test
    void endsWithEmptySuffix() {
        assertEquals(true, eval("'anything' ends with ''"));
    }

    @Test
    void startsWithFullMatch() {
        assertEquals(true, eval("'exact' starts with 'exact'"));
        assertEquals(true, eval("'exact' ends with 'exact'"));
    }

    @Test
    void startsWithLongerPatternIsFalse() {
        assertEquals(false, eval("'ab' starts with 'abc'"));
        assertEquals(false, eval("'ab' ends with 'abc'"));
    }

    @Test
    void worksInIfCondition() {
        String out = exec("""
                msg = '/help'
                if msg starts with '/'
                  print('command')
                end
                """);
        assertEquals("command\n", out);
    }

    @Test
    void worksWithFilenameSuffix() {
        String out = exec("""
                file = 'notes.notch'
                if file ends with '.notch'
                  print('notch file')
                end
                """);
        assertEquals("notch file\n", out);
    }

    @Test
    void chainedStartsWith() {
        //('abc' starts with 'ab') starts with 't'
        assertEquals(true, eval("'abc' starts with 'ab' starts with 't'"));
    }
}