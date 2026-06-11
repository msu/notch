package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;

public class NotchReturn extends NotchStatement {
    private final NotchExpression value;

    public NotchReturn(Span span, NotchExpression value) {
        super(span);
        this.value = addChild(value);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object v = NotchRuntime.UNDEFINED;
        if (value != null) v = runtime.evaluate(value);
        throw new ReturnSignal(v);
    }

    public static final class ReturnSignal extends RuntimeException {
        public final Object value;

        public ReturnSignal(Object value) {
            this.value = value;
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
