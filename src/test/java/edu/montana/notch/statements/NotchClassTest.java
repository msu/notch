package edu.montana.notch.statements;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotchClassTest {

    @Test
    void constructAndReadHeaderField() {
        String out = exec("""
                class Point(x, y)
                end
                p = new Point(1, 2)
                print(p.x)
                """);
        assertEquals("1\n", out);
    }

    @Test
    void methodUsesThis() {
        String out = exec("""
                class Point(x, y)
                  function dist()
                    return this.x + this.y
                  end
                end
                p = new Point(1, 2)
                print(p.dist())
                """);
        assertEquals("3\n", out);
    }

    @Test
    void bodyFieldReferencingHeader() {
        String out = exec("""
                class Point(x, y)
                  field sum = x + y
                end
                p = new Point(2, 3)
                print(p.sum)
                """);
        assertEquals("5\n", out);
    }

    @Test
    void bodyFieldDefaultsWhenNoInitializer() {
        String out = exec("""
                class Box(w)
                  field label
                end
                b = new Box(4)
                print(b.label)
                """);
        assertEquals("<undefined>\n", out);
    }

    @Test
    void declaredFieldWriteThenRead() {
        String out = exec("""
                class Point(x, y)
                  field label = 'pt'
                end
                p = new Point(1, 2)
                p.label = 'origin'
                print(p.label)
                """);
        assertEquals("origin\n", out);
    }

    @Test
    void methodCanMutateThisField() {
        String out = exec("""
                class Counter(n)
                  function bump()
                    this.n = this.n + 1
                  end
                end
                c = new Counter(0)
                c.bump()
                print(c.n)
                """);
        assertEquals("1\n", out);
    }

    @Test
    void writingUndeclaredFieldGivesClearError() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                class Point(x, y)
                end
                p = new Point(1, 2)
                p.bogus = 5
                """));
        assertTrue(ex.getMessage().contains("has no field 'bogus'"),
                "expected a no-such-field error, got: " + ex.getMessage());
    }

    @Test
    void misspelledFieldSuggestsClosestDeclaredField() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                class Point(x, y)
                  field health = 20
                end
                p = new Point(1, 2)
                p.helth = 5
                """));
        assertTrue(ex.getMessage().contains("Did you mean 'health'?"),
                "expected a 'did you mean' suggestion, got: " + ex.getMessage());
    }

    @Test
    void unrelatedFieldWriteOffersNoSuggestion() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                class Point(x, y)
                  field health = 20
                end
                p = new Point(1, 2)
                p.somethingEntirelyDifferent = 5
                """));
        assertTrue(ex.getMessage().contains("has no field"),
                "expected a no-such-field error, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("Did you mean"),
                "a far-off name should not produce a suggestion, got: " + ex.getMessage());
    }

    @Test
    void newOnNonClassThrows() {
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                notAClass = 5
                x = new notAClass()
                """));
        assertTrue(ex.getMessage().contains("is a value, not a class"),
                "expected a not-a-class error, got: " + ex.getMessage());
    }

    @Test
    void instantiatingUndeclaredClassThrows() {
        var ex = assertThrows(RuntimeException.class, () -> exec("p = new Point(1, 2)"));
        assertTrue(ex.getMessage().contains("no class named 'Point'"),
                "expected a no-such-class error, got: " + ex.getMessage());
    }

    @Test
    void instantiatingBeforeDeclarationThrows() {
        // Classes are not hoisted
        var ex = assertThrows(RuntimeException.class, () -> exec("""
                p = new Point(1, 2)
                class Point(x, y)
                end
                """));
        assertTrue(ex.getMessage().contains("no class named 'Point'"),
                "expected a no-such-class error (no hoisting), got: " + ex.getMessage());
    }

    @Test
    void cannotAssignToThis() {
        var ex = assertThrows(RuntimeException.class, () -> exec("this = 5"));
        assertTrue(ex.getMessage().contains("cannot assign to 'this'"),
                "expected a this-reassignment error, got: " + ex.getMessage());
    }
}
