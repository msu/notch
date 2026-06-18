package edu.montana.notch.chisel;

import edu.montana.notch.Notch;
import edu.montana.notch.NotchParser;
import edu.montana.notch.expressions.NotchExpression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpanTest {

    private static NotchExpression parse(String src) {
        Source source = new Source("<span-test>", src);
        TokenStream tokens = Notch.TOKENIZER.tokenize(source);
        return new NotchParser(tokens).parseExpression();
    }

    @Test
    void comparisonSpanCoversBothOperands() {
        NotchExpression expr = parse("a < b");
        assertEquals("a < b", expr.rawContent());
        assertEquals(5, expr.end().index);
    }
}