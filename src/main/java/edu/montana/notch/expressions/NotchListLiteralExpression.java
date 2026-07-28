package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.BetterList;

import java.util.List;

public class NotchListLiteralExpression extends NotchExpression {
    public final List<NotchExpression> values;

    public NotchListLiteralExpression(Span span, List<NotchExpression> values) {
        super(span);
        this.values = values;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        BetterList<Object> betterList = new BetterList<>();
        for (NotchExpression expression : values) {
            betterList.add(runtime.evaluate(expression));
        }
        return betterList;
    }
}

