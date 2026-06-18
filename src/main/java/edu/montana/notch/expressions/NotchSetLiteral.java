package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.BetterSet;

import java.util.List;

public class NotchSetLiteral extends NotchExpression {
    public final List<NotchExpression> values;

    public NotchSetLiteral(Span span, List<NotchExpression> values) {
        super(span);
        this.values = values;

        addChildren(values);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        BetterSet<Object> set = new BetterSet<>();
        for (NotchExpression expression : values) {
            set.add(runtime.evaluate(expression));
        }
        return set;
    }
}
