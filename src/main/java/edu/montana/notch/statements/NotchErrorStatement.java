package edu.montana.notch.statements;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;

public class NotchErrorStatement extends NotchStatement {
    public final Diagnostic diagnostic;

    public NotchErrorStatement(Span span, Diagnostic diagnostic) {
        super(span);
        this.diagnostic = diagnostic;
    }

    @Override
    public void execute(NotchRuntime runtime) {
        throw new NotchRuntimeException(runtime.currentStackTrace(), diagnostic);
    }
}
