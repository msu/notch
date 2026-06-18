package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.BetterMap;

import java.util.List;

public class NotchMapLiteral extends NotchExpression {
    public final List<NotchExpression> keys;
    public final List<NotchExpression> values;

    public NotchMapLiteral(Span span, List<NotchExpression> keys, List<NotchExpression> values) {
        super(span);
        this.keys = keys;
        this.values = values;

        addChildren(keys);
        addChildren(values);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        BetterMap<Object, Object> betterMap = new BetterMap<>();
        for (int i = 0; i < keys.size(); i++) {
            betterMap.put(runtime.evaluate(keys.get(i)), runtime.evaluate(values.get(i)));
        }
        return betterMap;
    }
}
