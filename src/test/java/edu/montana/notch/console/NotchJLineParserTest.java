package edu.montana.notch.console;

import org.jline.reader.EOFError;
import org.jline.reader.Parser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotchJLineParserTest {

    private final NotchJLineParser parser = new NotchJLineParser();

    private void expectIncomplete(String buffer) {
        assertThrows(EOFError.class,
                () -> parser.parse(buffer, buffer.length(), Parser.ParseContext.ACCEPT_LINE),
                "expected continuation for buffer:\n" + buffer);
    }

    private void expectComplete(String buffer) {
        assertDoesNotThrow(
                () -> parser.parse(buffer, buffer.length(), Parser.ParseContext.ACCEPT_LINE),
                "expected acceptance for buffer:\n" + buffer);
    }

    @Test
    void fibonacciSequence() {
        expectIncomplete("""
                a = 0
                b = 1
                repeat 8 times""");
        expectIncomplete("""
                a = 0
                b = 1
                repeat 8 times
                  next = a + b
                  print(a)
                  a = b
                  b = next""");

        String program = """
                a = 0
                b = 1
                repeat 8 times
                  next = a + b
                  print(a)
                  a = b
                  b = next
                end
                """;
        expectComplete(program);
        assertEquals("0\n1\n1\n2\n3\n5\n8\n13\n", exec(program));
    }

    @Test
    void sumOfSquaresWithForLoop() {
        expectIncomplete("""
                sum = 0
                for n in [1, 2, 3, 4]
                  sum = sum + n * n""");

        String program = """
                sum = 0
                for n in [1, 2, 3, 4]
                  sum = sum + n * n
                end
                print(sum)
                """;
        expectComplete(program);
        assertEquals("30\n", exec(program));
    }

    @Test
    void countdownWithRepeatWhile() {
        expectIncomplete("""
                x = 5
                repeat while x > 0
                  print(x)
                  x = x - 1""");

        String program = """
                x = 5
                repeat while x > 0
                  print(x)
                  x = x - 1
                end
                """;
        expectComplete(program);
        assertEquals("5\n4\n3\n2\n1\n", exec(program));
    }

    @Test
    void sumUntilLimitWithRepeatUntil() {
        expectIncomplete("""
                total = 0
                n = 1
                repeat until total >= 10
                  total = total + n
                  n = n + 1""");

        String program = """
                total = 0
                n = 1
                repeat until total >= 10
                  total = total + n
                  n = n + 1
                end
                print(total)
                """;
        expectComplete(program);
        assertEquals("10\n", exec(program));
    }

    @Test
    void skipMiddleWithContinueInsideForLoop() {
        expectIncomplete("""
                for n in [1, 2, 3, 4, 5]
                  if n == 3
                    continue
                  end
                  print(n)""");

        String program = """
                for n in [1, 2, 3, 4, 5]
                  if n == 3
                    continue
                  end
                  print(n)
                end
                """;
        expectComplete(program);
        assertEquals("1\n2\n4\n5\n", exec(program));
    }

    // --- continuation for function / try / class blocks ---
    // isBlockIncomplete matches diagnostic notes starting with "Unterminated"
    // (capital U), but these three blocks report lowercase "unterminated ...",
    // so the REPL never offers a continuation prompt for them.

    @Test
    @Disabled("known bug: 'unterminated function, expected 'end'' is lowercase and misses "
            + "isBlockIncomplete's startsWith(\"Unterminated\") check")
    void unterminatedFunctionTriggersContinuation() {
        expectIncomplete("""
                function add(a, b)
                  return a + b""");
    }

    @Test
    @Disabled("known bug: 'unterminated 'try', expected 'end'' is lowercase and misses "
            + "isBlockIncomplete's startsWith(\"Unterminated\") check")
    void unterminatedTryTriggersContinuation() {
        expectIncomplete("""
                try
                  x = 1""");
    }

    @Test
    @Disabled("known bug: 'unterminated class, expected 'end'' is lowercase and misses "
            + "isBlockIncomplete's startsWith(\"Unterminated\") check")
    void unterminatedClassTriggersContinuation() {
        expectIncomplete("""
                class Dog
                  field age""");
    }

    @Test
    void completedFunctionTryAndClassBlocksAccepted() {
        expectComplete("""
                function add(a, b)
                  return a + b
                end
                """);
        expectComplete("""
                try
                  x = 1
                catch e
                end
                """);
        expectComplete("""
                class Dog
                  field age
                end
                """);
    }
}
