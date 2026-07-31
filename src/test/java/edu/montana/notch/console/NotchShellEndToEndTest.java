package edu.montana.notch.console;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NotchShellEndToEndTest {

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

    // ----- Multi-line programs: continuation must trigger -----

    @Test
    @EnabledIf("jarExists")
    void fibonacciViaRealShell() throws Exception {
        var out = runShell("""
                a = 0
                b = 1
                repeat 8 times
                  next = a + b
                  print(a)
                  a = b
                  b = next
                end
                exit
                """);
        assertTrue(out.contains("0\n1\n1\n2\n3\n5\n8\n13\n"),
                "expected Fibonacci sequence. Full output:\n" + out);
    }

    @Test
    @EnabledIf("jarExists")
    void multilineForLoopProducesExpectedOutput() throws Exception {
        var out = runShell("""
                for x in [10, 20, 30]
                  print(x * 2)
                end
                exit
                """);
        assertTrue(out.contains("20\n40\n60\n"),
                "expected doubled values. Full output:\n" + out);
    }

    @Test
    @EnabledIf("jarExists")
    void multilineRepeatWhileCounts() throws Exception {
        var out = runShell("""
                x = 1
                repeat while x < 4
                  print(x)
                  x = x + 1
                end
                exit
                """);
        assertTrue(out.contains("1\n2\n3\n"),
                "expected counts 1..3. Full output:\n" + out);
    }

    @Test
    @EnabledIf("jarExists")
    void multilineRepeatUntilAccumulates() throws Exception {
        // The final bare `total` expression is how the REPL inspects a value
        // (parse() prefers expression-parse at the top level, so print(total)
        // would parse as a method call on the identifier `print`).
        var out = runShell("""
                total = 0
                n = 1
                repeat until total >= 10
                  total = total + n
                  n = n + 1
                end
                total
                exit
                """);
        assertTrue(out.contains("10"),
                "expected accumulated total 10. Full output:\n" + out);
    }

    @Test
    @EnabledIf("jarExists")
    void multilineIfElseRunsCorrectBranch() throws Exception {
        var out = runShell("""
                x = 5
                if x > 3
                  print('big')
                else
                  print('small')
                end
                exit
                """);
        assertTrue(out.contains("big"),
                "expected 'big' branch output. Full output:\n" + out);
    }

    @Test
    @EnabledIf("jarExists")
    void nestedRepeatInsideForExecutesCorrectly() throws Exception {
        var out = runShell("""
                for x in [1, 2]
                  repeat 2 times
                    print(x)
                  end
                end
                exit
                """);
        assertTrue(out.contains("1\n1\n2\n2\n"),
                "expected nested loop output. Full output:\n" + out);
    }

    @Test
    @EnabledIf("jarExists")
    void breakAndContinueWorkInsideContinuation() throws Exception {
        var out = runShell("""
                for x in [1, 2, 3, 4, 5]
                  if x == 2
                    continue
                  end
                  if x == 4
                    break
                  end
                  print(x)
                end
                exit
                """);
        assertTrue(out.contains("1\n3\n"),
                "expected only 1 and 3 printed (continue skips 2, break exits at 4). Full output:\n" + out);
    }

    // ----- Single-line programs: continuation must NOT trigger -----

    @Test
    @EnabledIf("jarExists")
    void singleLineCompleteScriptRunsImmediately() throws Exception {
        // Bare expression at the top level — parse() picks expression-mode,
        // evaluates 1 + 2, and the REPL prints the value.
        var out = runShell("""
                1 + 2
                exit
                """);
        assertTrue(out.contains("3"),
                "expected '3' from arithmetic expression. Full output:\n" + out);
    }

    @Test
    @EnabledIf("jarExists")
    void singleLineCompleteRepeatRunsWithoutWaitingForMore() throws Exception {
        // Whole repeat block on one line — must NOT trigger continuation;
        // the program should execute and 'exit' should be reached.
        var out = runShell("""
                repeat 3 times print(it) end
                exit
                """);
        assertTrue(out.contains("1\n2\n3\n"),
                "expected 1,2,3 from single-line repeat. Full output:\n" + out);
    }

    @Test
    @EnabledIf("jarExists")
    void undefinedIdentifierDoesNotHangShell() throws Exception {
        // The Option-A wart fix: a bare unrecognized identifier at the REPL
        // used to trigger continuation because the parser threw ParseException
        // at EOF. Now the shell evaluates the identifier (yielding <undefined>)
        // and reaches the exit command on the next line. We use a clearly-
        // unbound name since `print` is now a keyword (no longer an identifier).
        var out = runShell("""
                undefined_thing
                exit
                """);
        assertTrue(out.contains("Goodbye"),
                "shell did not reach the exit command. Full output:\n" + out);
    }

    @Test
    @EnabledIf("jarExists")
    void callingUndefinedFunctionReportsError() throws Exception {
        var out = runShell("""
                add()
                exit
                """);
        assertTrue(out.contains("undefined function 'add'"),
                "expected error message for undefined function. Full output:\n" + out);
    }

    @Test
    @EnabledIf("jarExists")
    void printAtTopLevelOutputsValue() throws Exception {
        // The headline behavior: print(42) at the REPL top level should print
        // 42. Before the keyword promotion this would output <undefined>
        // because `print` was tokenized as an unbound identifier and the
        // expression-parse path won via parse()'s two-pass. After promotion,
        // expression-parse rejects `print`, falls back to statement-parse,
        // and parsePrintStatement runs.
        var out = runShell("""
                print(42)
                exit
                """);
        assertTrue(out.contains("42"),
                "expected '42' from top-level print. Full output:\n" + out);
    }

    // ----- load command -----

    @Test
    @EnabledIf("jarExists")
    void loadExecutesFileAndPrintsConfirmation(@TempDir Path tempDir) throws Exception {
        Path script = tempDir.resolve("hello.notch");
        Files.writeString(script, "print(42)\n");

        var out = runShell("load " + script.toAbsolutePath() + "\nexit\n");
        assertTrue(out.contains("42"),
                "expected file output. Full output:\n" + out);
        assertTrue(out.contains("loaded"),
                "expected load confirmation message. Full output:\n" + out);
    }

    @Test
    @EnabledIf("jarExists")
    void loadReportsFileNotFoundAndShellContinues(@TempDir Path tempDir) throws Exception {
        Path missing = tempDir.resolve("missing.notch");

        var out = runShell("load " + missing.toAbsolutePath() + "\nexit\n");
        assertTrue(out.contains("file not found"),
                "expected not-found message. Full output:\n" + out);
        assertTrue(out.contains("Goodbye"),
                "expected shell to continue after load error. Full output:\n" + out);
    }

    // ----- error recovery -----

    @Test
    @EnabledIf("jarExists")
    void shellContinuesAfterRuntimeError() throws Exception {
        var out = runShell("""
                add()
                print(99)
                exit
                """);
        assertTrue(out.contains("undefined function 'add'"),
                "expected error message for undefined function. Full output:\n" + out);
        assertTrue(out.contains("99"),
                "expected shell to continue and execute next line after error. Full output:\n" + out);
    }
}
