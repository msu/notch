package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;
import java.util.Objects;

public class NotchRepeatWhile extends NotchLoop {

    public final NotchExpression cond;

    public NotchRepeatWhile(Span span, NotchExpression cond, List<NotchStatement> body) {
        super(span, body);
        this.cond = Objects.requireNonNull(cond);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        try (var lock = runtime.pushScope()) {
            while (runtime.isTruthy(runtime.evaluate(cond))) {
                if (runBody(runtime) == Control.BREAK) return;
            }
        }
    }
}
