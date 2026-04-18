package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.util.BetterList;
import bigsky.notch.chisel.Location;

import java.util.List;
import java.util.Objects;

public class NotchListLiteral extends NotchExpression {
    public final List<NotchExpression> values;

    public NotchListLiteral(String fileId, Location start, List<NotchExpression> values, Location end) {
        super(fileId, start, end);
        this.values = Objects.requireNonNull(values);
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

