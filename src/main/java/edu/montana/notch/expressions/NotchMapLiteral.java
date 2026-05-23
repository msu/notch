package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.BetterMap;

import java.util.Map;

public class NotchMapLiteral extends NotchExpression {
    private final Map<String, NotchExpression> mapValues;

    public NotchMapLiteral(Span span, Map<String, NotchExpression> mapValues) {
        super(span);
        this.mapValues = Map.copyOf(mapValues);
        addChildren(mapValues.values());
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        BetterMap<Object, Object> betterMap = new BetterMap<>();
        for (Map.Entry<String, NotchExpression> s : mapValues.entrySet()) {
            String key = s.getKey();
            Object val = runtime.evaluate(s.getValue());
            betterMap.put(key, val);
        }
        return betterMap;
    }
}
