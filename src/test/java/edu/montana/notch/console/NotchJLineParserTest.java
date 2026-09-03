package edu.montana.notch.console;

import org.jline.reader.EOFError;
import org.jline.reader.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    void everyBlockFormContinuesUntilItsEnd() {
        expectIncomplete("repeat 8 times\n  print(1)");
        expectIncomplete("for n in [1, 2]\n  print(n)");
        expectIncomplete("if x\n  print(1)");
        expectIncomplete("function add(a, b)\n  return a + b");
        expectIncomplete("try\n  x = 1");
        expectIncomplete("class Dog\n  field age");
    }

    @Test
    void closedBlocksAreAccepted() {
        expectComplete("repeat 8 times\n  print(1)\nend\n");
        expectComplete("for n in [1, 2]\n  print(n)\nend\n");
        expectComplete("if x\n  print(1)\nend\n");
        expectComplete("function add(a, b)\n  return a + b\nend\n");
        expectComplete("try\n  x = 1\ncatch e\nend\n");
        expectComplete("class Dog\n  field age\nend\n");
    }

    @Test
    void anOpenBlockAfterCompletedStatementsStillContinues() {
        expectIncomplete("""
                a = 0
                b = 1
                repeat 8 times
                  a = b""");
    }

    @Test
    void aClosedInnerBlockDoesNotMaskAnOpenOuterOne() {
        expectIncomplete("""
                for n in [1, 2, 3]
                  if n == 3
                    continue
                  end
                  print(n)""");
    }

    @Test
    void unclosedQuoteContinues() {
        expectIncomplete("x = 'abc");
        expectIncomplete("x = \"abc");
    }

    @Test
    void unclosedBracketContinues() {
        expectIncomplete("print(");
        expectIncomplete("x = [1,");
        expectIncomplete("x = {");
    }

    @Test
    void otherParseContextsNeverAskForMore() {
        String open = "repeat 8 times";
        assertDoesNotThrow(
                () -> parser.parse(open, open.length(), Parser.ParseContext.COMPLETE),
                "only ACCEPT_LINE should trigger a continuation prompt");
    }
}
