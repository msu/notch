package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchContinue extends NotchStatement {
    public static final ContinueSignal SIGNAL = new ContinueSignal();

    public NotchContinue(Span span) {
        super(span);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        throw SIGNAL;
    }

    public static final class ContinueSignal extends RuntimeException {
        private ContinueSignal() {}

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
