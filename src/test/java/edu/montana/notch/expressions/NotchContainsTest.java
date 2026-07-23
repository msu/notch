package edu.montana.notch.expressions;

import edu.montana.notch.util.BetterList;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchContainsTest {

    @Test
    void stringSubstring() {
        assertEquals(true, eval("'hello world' contains 'world'"));
        assertEquals(false, eval("'hello world' contains 'banana'"));
    }

    @Test
    void stringContainsEmpty() {
        assertEquals(true, eval("'hello' contains ''"));
    }

    @Test
    void listContainsElement() {
        BetterList<Integer> items = new BetterList<>(Arrays.asList(1, 2, 3));
        assertEquals(true, eval("items contains 2", "items", items));
        assertEquals(false, eval("items contains 99", "items", items));
    }

    @Test
    void arrayContainsElement() {
        Integer[] arr = {10, 20, 30};
        assertEquals(true, eval("arr contains 20", "arr", arr));
        assertEquals(false, eval("arr contains 99", "arr", arr));
    }

    @Test
    void mapContainsKey() {
        var map = new HashMap<String, Object>();
        map.put("debug", true);
        map.put("name", "alice");
        assertEquals(true, eval("cfg contains 'debug'", "cfg", map));
        assertEquals(false, eval("cfg contains 'missing'", "cfg", map));
    }

    @Test
    void nullLhsReturnsFalse() {
        assertEquals(false, eval("x contains 'a'", "x", null));
    }

    @Test
    void numericLhsReturnsFalse() {
        assertEquals(false, eval("5 contains 1"));
    }

    @Test
    void worksInIfCondition() {
        String out = exec("""
                words = ['spawn', 'admin', 'help']
                if words contains 'spawn'
                  print('found')
                end
                """, "words", new BetterList<>(Arrays.asList("spawn", "admin", "help")));
        assertEquals("found\n", out);
    }

    @Test
    void worksOnChatMessage() {
        String out = exec("""
                msg = 'hello spawn world'
                if msg contains 'spawn'
                  print('flagged')
                end
                """);
        assertEquals("flagged\n", out);
    }
}
