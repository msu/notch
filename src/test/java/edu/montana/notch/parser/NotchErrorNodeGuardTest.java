package edu.montana.notch.parser;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.expressions.NotchErrorExpression;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.statements.NotchErrorStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotchErrorNodeGuardTest {

    private static final Source SOURCE = new Source("<test>", "");

    @Test
    void notchErrorStatementThrows() {
        var diag = new Diagnostic().note("test error");
        var stmt = new NotchErrorStatement(SOURCE.span, diag);
        var runtime = new NotchRuntime(SOURCE);
        var ex = assertThrows(NotchRuntimeException.class, () -> stmt.execute(runtime));
        assertSame(diag, ex.diagnostic);
    }

    @Test
    void notchErrorExpressionThrows() {
        var diag = new Diagnostic().note("test error");
        var expr = new NotchErrorExpression(SOURCE.span, diag);
        var runtime = new NotchRuntime(SOURCE);
        var ex = assertThrows(NotchRuntimeException.class, () -> expr.evaluate(runtime));
        assertSame(diag, ex.diagnostic);
    }
}
