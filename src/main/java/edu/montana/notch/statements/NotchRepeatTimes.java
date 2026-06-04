package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;
import java.util.Objects;

public class NotchRepeatTimes extends NotchLoop {

    public final NotchExpression count;

    public NotchRepeatTimes(Span span, NotchExpression count, List<NotchStatement> body) {
        super(span, body);
        this.count = Objects.requireNonNull(count);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object value = runtime.evaluate(count);
        int n = ((Number) value).intValue();
        if (n < 1) return;
        try (var lock = runtime.pushScope()) {
            for (int i = 1; i <= n; i++) {
                lock.define("it", i);
                if (runBody(runtime) == Control.BREAK) return;
            }
        }
    }
}
