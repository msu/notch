package edu.montana.notch.runtime;

import edu.montana.notch.statements.NotchBreak;
import edu.montana.notch.statements.NotchContinue;
import edu.montana.notch.statements.NotchStatement;

import java.util.List;

public final class LoopBody {
    private LoopBody() {}

    public enum Control { CONTINUE, BREAK }

    public static Control runIteration(NotchRuntime runtime, List<NotchStatement> body) {
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
