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

class NotchErrorNodeTest {

    private static final Source SOURCE = new Source("<test>", "");

    @Test
    void notchErrorStatementThrowsItsDiagnostic() {
        var diag = new Diagnostic().note("test error");
        var stmt = new NotchErrorStatement(SOURCE.span, diag);
        var runtime = new NotchRuntime(SOURCE);
        assertSame(diag, assertThrows(NotchRuntimeException.class,
                () -> stmt.execute(runtime)).diagnostic);
    }

    @Test
    void notchErrorExpressionThrowsItsDiagnostic() {
        var diag = new Diagnostic().note("test error");
        var expr = new NotchErrorExpression(SOURCE.span, diag);
        var runtime = new NotchRuntime(SOURCE);
        assertSame(diag, assertThrows(NotchRuntimeException.class,
                () -> expr.evaluate(runtime)).diagnostic);
    }
}
