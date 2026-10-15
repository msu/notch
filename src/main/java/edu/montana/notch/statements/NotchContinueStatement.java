package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchControlFlow;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchContinueStatement extends NotchStatement {
    public NotchContinueStatement(Span span) {
        super(span);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        throw NotchControlFlow.Continue.INST;
    }

}
