package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;
import java.util.Objects;

import static edu.montana.notch.runtime.LoopBody.Control.BREAK;
import static edu.montana.notch.runtime.LoopBody.runIteration;

public class NotchRepeatUntil extends NotchStatement {

    public final NotchExpression cond;
    public final List<NotchStatement> body;

    public NotchRepeatUntil(Span span, NotchExpression cond, List<NotchStatement> body) {
        super(span);
        this.cond = Objects.requireNonNull(cond);
        this.body = List.copyOf(Objects.requireNonNull(body));
    }

    @Override
    public void execute(NotchRuntime runtime) {
        try (var lock = runtime.pushScope()) {
            while (!runtime.isTruthy(runtime.evaluate(cond))) {
                if (runIteration(runtime, body) == BREAK) return;
            }
        }
    }
}
