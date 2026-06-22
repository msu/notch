package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.Exceptions;

public class NotchRethrow extends NotchStatement {
    public NotchRethrow(Span span) {
        super(span);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Throwable current = runtime.currentHandled();
        if (current == null) {
            throw new IllegalStateException("'rethrow' with no exception being handled");
        }
        throw Exceptions.rethrow(current);
    }
}
