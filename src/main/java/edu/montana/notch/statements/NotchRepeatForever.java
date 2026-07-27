package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;

public class NotchRepeatForever extends NotchLoop {
    public NotchRepeatForever(Span span, List<NotchStatement> body) {
        super(span, body);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        try (var ignored = runtime.pushScope()) {
            while (true) {
                if (runBody(runtime) == Control.BREAK) return;
            }
        }
    }
}
