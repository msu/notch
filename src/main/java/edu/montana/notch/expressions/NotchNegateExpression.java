package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.util.BetterMath;

import java.util.Objects;

public class NotchNegateExpression extends NotchExpression {
    public final NotchExpression expression;

    public NotchNegateExpression(Span span, NotchExpression expression) {
        super(span);
        this.expression = Objects.requireNonNull(expression);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var val = runtime.evaluate(expression);
        if (val instanceof Number n) {
            return BetterMath.safeMul(-1, n);
        } else {
            var diag = new Diagnostic();
            diag.highlight(span());
            diag.note("cannot negate %s".formatted(NotchRuntime.className(val)));
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
    }
}
