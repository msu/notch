package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;

import java.util.List;
import java.util.Objects;

public abstract class NotchLoop extends NotchStatement {

    public final List<NotchStatement> body;

    protected NotchLoop(Span span, List<NotchStatement> body) {
        super(span);
        this.body = List.copyOf(Objects.requireNonNull(body));
    }

    protected enum Control { CONTINUE, BREAK }

    protected final Control runBody(NotchRuntime runtime) {
        for (NotchStatement stmt : body) {
            try {
                runtime.execute(stmt);
            } catch (NotchRuntimeException nre) {
                Throwable cause = nre.getCause();
                if (cause instanceof NotchBreak.BreakSignal) return Control.BREAK;
                if (cause instanceof NotchContinue.ContinueSignal) return Control.CONTINUE;
                throw nre;
            }
        }
        return Control.CONTINUE;
    }
}
