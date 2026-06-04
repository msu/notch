package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchBreak extends NotchStatement {
    public static final BreakSignal SIGNAL = new BreakSignal();

    public NotchBreak(Span span) {
        super(span);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        throw SIGNAL;
    }

    public static final class BreakSignal extends RuntimeException {
        private BreakSignal() {}

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
