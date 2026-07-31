package edu.montana.notch.console;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies every literal and operator from the docs reference sheet
 * ({@code docs/Reference-Sheet.md}, the Literals and Operators sections)
 * evaluates correctly through the real REPL. Each case below mirrors a line
 * from those two sections, so the arrays double as a completeness checklist:
 * if the language grows a literal or operator, add it to the reference sheet
 * and to the matching array here.
 *
 * <p>Runs against the built jar via stdin, so it exercises the jline line
 * reader too (e.g. the {@code !} inverse operator, which shell history
 * expansion would otherwise swallow).
 */
class ReferenceSheetReplTest {

    // Each row is {expression, expected REPL output}. Expressions are made
    // self-contained (concrete values instead of the reference sheet's
    // placeholder identifiers) so each prints exactly one result line.

    private static final String[][] LITERALS = {
            {"42", "42"},                       // integer
            {"0xff", "255"},                    // hex
            {"0b101", "5"},                     // binary
            {"0o77", "63"},                     // octal
            {"true", "true"},                   // boolean
            {"false", "false"},
            {"null", "null"},                   // null literal
            {"'hello'", "hello"},               // single-quoted string
            {"\"hello\"", "hello"},             // double-quoted string
            {":hello", "hello"},                // terse string
            {"[1, 2, 3]", "[1, 2, 3]"},         // list
            {"{'foo' -> 1}", "{foo=1}"},        // map (single entry: BetterMap is HashMap-backed,
                                                //      so multi-entry print order is not stable)
            {"{}", "{}"},                       // empty map
            {"{1, 2, 3}", "[1, 2, 3]"},         // set (prints like a list)
            {"{,}", "[]"},                      // empty set
            {"(1 + 2) * 3", "9"},               // parens group
    };

    private static final String[][] OPERATORS = {
            {"['  hi  '][0].trim()", "hi"},                 // index + property/call
            {"-5", "-5"},                                   // negate
            {"!true", "false"},                             // inverse (symbol)
            {"not true", "false"},                          // inverse (keyword)
            {"2 * 3", "6"},                                 // multiplication
            {"10 / 2", "5"},                                // division
            {"7 % 4", "3"},                                 // remainder
            {"1 + 2", "3"},                                 // addition
            {"4 - 1", "3"},                                 // subtraction
            {"2 < 3", "true"},                              // less
            {"3 > 2", "true"},                              // greater
            {"2 <= 2", "true"},                             // less-or-equal
            {"3 >= 3", "true"},                             // greater-or-equal
            {"'notch' starts with 'no'", "true"},           // starts with
            {"'notch' ends with 'ch'", "true"},             // ends with
            {"'notch' contains 'otc'", "true"},             // contains
            {"1 == 1", "true"},                             // equality
            {"1 != 2", "true"},                             // inequality
            {"1 is 1", "true"},                             // is
            {"2 is not 1", "true"},                         // is not
            {"'' is empty", "true"},                        // is empty
            {"[1] is not empty", "true"},                   // is not empty
            {"true && true", "true"},                       // logical and (symbol)
            {"true and true", "true"},                      // logical and (keyword)
            {"false || true", "true"},                      // logical or (symbol)
            {"false or true", "true"},                      // logical or (keyword)
            {"null ?: 'default'", "default"},               // fallback
            {"'yes' if true else 'no'", "yes"},             // conditional
    };

    @Test
    @EnabledIf("jarExists")
    void allLiteralsEvaluateInRepl() throws Exception {
        assertReplResults(LITERALS);
    }

    @Test
    @EnabledIf("jarExists")
    void allOperatorsEvaluateInRepl() throws Exception {
        assertReplResults(OPERATORS);
    }

    /**
     * Exercises every statement form from the reference sheet in one REPL
     * session. Each form prints a uniquely-tagged line, so the assertions
     * double as a completeness check. Because `function`/`class`/`try` are fed
     * multi-line, this also verifies REPL block continuation for them.
     */
    @Test
    @EnabledIf("jarExists")
    void allStatementFormsWorkInRepl() throws Exception {
        var out = runShell("""
                x = 5
                print('assign=' + x)
                list = [1, 2, 3]
                list[0] = 9
                print('index=' + list[0])
                if true print('if=yes') else print('if=no') end
                if false print('noelse=no') end
                for c in 'ab' print('for=' + c) end
                for c in 'ab' index i print('idx=' + i) end
                repeat 2 times print('rep=' + it) end
                n = 0
                repeat while n < 2 n = n + 1 print('while=' + n) end
                m = 0
                repeat until m >= 2 m = m + 1 print('until=' + m) end
                for k in [1, 2, 3, 4]
                  if k == 2 continue end
                  if k == 4 break end
                  print('loop=' + k)
                end
                function dbl(v)
                  return v * 2
                end
                print('fn=' + dbl(21))
                class Box
                  field v
                  function get()
                    return this.v
                  end
                end
                b = new Box()
                b.v = 7
                print('cls=' + b.get())
                import java.time.LocalDate
                print('import=ok')
                try
                  throw 'boom'
                catch RuntimeException as e
                  print('try=' + e)
                end
                exit
                """);

        for (String expected : new String[]{
                "assign=5", "index=9", "if=yes", "for=a", "for=b", "idx=0", "idx=1",
                "rep=1", "rep=2", "while=1", "while=2", "until=1", "until=2",
                "loop=1", "loop=3", "fn=42", "cls=7", "import=ok", "try=boom"}) {
            assertTrue(out.contains(expected),
                    "missing statement output `" + expected + "`. Full output:\n" + out);
        }
        assertTrue(!out.contains("if=no") && !out.contains("noelse=no")
                        && !out.contains("loop=2") && !out.contains("loop=4"),
                "unexpected branch/loop output. Full output:\n" + out);
    }

    /**
     * Regression for the capitalized-vs-lowercase "unterminated" bug: multi-line
     * `function`/`class`/`try` must trigger the continuation prompt, not error out.
     */
    @Test
    @EnabledIf("jarExists")
    void multilineBlocksContinueInRepl() throws Exception {
        var out = runShell("""
                function g(a)
                  return a + 1
                end
                g(41)
                exit
                """);
        assertTrue(out.contains("42"), "multi-line function should continue and evaluate. Output:\n" + out);
        assertTrue(!out.toLowerCase().contains("unterminated"),
                "multi-line function must not report 'unterminated'. Output:\n" + out);
        assertTrue(!out.contains("outside a function"),
                "return inside the function must be accepted. Output:\n" + out);
    }

    /**
     * Feeds every expression to one REPL session, each followed by a unique
     * marker string, then asserts the line the REPL printed just before each
     * marker equals the expected value. Anchoring on markers keeps the check
     * robust against startup noise (the dumb-terminal warning, log notices).
     */
    private static void assertReplResults(String[][] cases) throws Exception {
        StringBuilder script = new StringBuilder();
        for (int i = 0; i < cases.length; i++) {
            script.append(cases[i][0]).append('\n');
            script.append("'@@").append(i).append("'").append('\n'); // marker
        }
        script.append("exit\n");

        String out = runShell(script.toString());
        List<String> results = resultLines(out);

        for (int i = 0; i < cases.length; i++) {
            String marker = "@@" + i;
            int idx = results.indexOf(marker);
            assertTrue(idx > 0,
                    "no REPL output found for `" + cases[i][0] + "` (marker " + marker
                            + " missing or first). Full output:\n" + out);
            assertEquals(cases[i][1], results.get(idx - 1),
                    "REPL result mismatch for `" + cases[i][0] + "`. Full output:\n" + out);
        }
    }

    /** Strips {@code notch > } prompts and drops blank/startup-noise lines. */
    private static List<String> resultLines(String out) {
        List<String> lines = new ArrayList<>();
        for (String raw : out.split("\\R")) {
            String line = raw.replace("notch > ", "").strip();
            if (line.isEmpty()
                    || line.startsWith("Goodbye")
                    || line.contains("WARN")
                    || line.contains("Terminal type")
                    || line.startsWith("Logs Available")
                    || line.startsWith("Notch:")) {
                continue;
            }
            lines.add(line);
        }
        return lines;
    }

    // ----- shell harness (mirrors NotchShellEndToEndTest) -----

    private static final Path JAR_PATH = Path.of("target/notch.jar");

    static boolean jarExists() {
        return Files.exists(JAR_PATH);
    }

    private static String runShell(String script) throws Exception {
        var javaBin = System.getProperty("java.home") + "/bin/java";
        var pb = new ProcessBuilder(
                javaBin,
                "--enable-native-access=ALL-UNNAMED",
                "-jar",
                JAR_PATH.toString()
        );
        pb.redirectErrorStream(true);
        var proc = pb.start();

        try (var writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write(script);
            writer.flush();
        }

        boolean exited = proc.waitFor(30, TimeUnit.SECONDS);
        if (!exited) {
            proc.destroyForcibly();
            var captured = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new AssertionError("notch shell did not exit within 30 seconds. Captured output:\n" + captured);
        }

        return new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
