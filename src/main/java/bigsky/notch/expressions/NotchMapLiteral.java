package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.BetterMap;
import bigsky.utils.chisel.Location;

import java.util.Map;

public class NotchMapLiteral extends NotchExpression {
    private Map<String, NotchExpression> mapValues;

    public NotchMapLiteral(String fileId, Location start, Location end) {
        super(fileId, start, end);
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

    public void setValues(Map<String, NotchExpression> mapValues) {
        addChildren(mapValues.values());
        this.mapValues = mapValues;
    }
}
