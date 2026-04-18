package bigsky.notch.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BetterEnvTest {
    public static void assertContains(String substring, String str) {
        assertTrue(str.contains(substring), "the substring %s is not in %s"
                .formatted(Text.repr(substring), Text.repr(str)));
    }

    @Test
    public void nonPublicOptionThrows() {
        class Env extends BetterEnv {
            Option<String> value = new Option<>("value", String.class);
        }

        var env = new Env();
        var ex = assertThrows(RuntimeException.class, env::load);
        var msg = ex.getMessage();
        assertContains("is not public", msg);
        assertContains("will not be parsed", msg);
        assertContains(Env.class.getName() + "#value", msg);
    }

    @Test
    public void missingOptionThrowsAnError() {
        class Env extends BetterEnv {
            public Option<String> value = new Option<>("value", String.class);
        }

        var env = new Env();
        var ex = assertThrows(RuntimeException.class, env::load);
        var msg = ex.getMessage();
        assertContains("no value or default-value specified", msg);
        assertContains("option \"value\"", msg);
    }
}
