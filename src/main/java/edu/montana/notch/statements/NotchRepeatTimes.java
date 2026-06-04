package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;
import java.util.Objects;

import static edu.montana.notch.runtime.LoopBody.Control.BREAK;
import static edu.montana.notch.runtime.LoopBody.runIteration;

public class NotchRepeatTimes extends NotchStatement {

    public final NotchExpression count;
    public final List<NotchStatement> body;

    public NotchRepeatTimes(Span span, NotchExpression count, List<NotchStatement> body) {
        super(span);
        this.count = Objects.requireNonNull(count);
        this.body = List.copyOf(Objects.requireNonNull(body));
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object value = runtime.evaluate(count);
        int n = ((Number) value).intValue();
        if (n < 1) return;
        try (var lock = runtime.pushScope()) {
            for (int i = 1; i <= n; i++) {
                lock.define("it", i);
                if (runIteration(runtime, body) == BREAK) return;
            }
        }
    }
}
