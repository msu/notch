package bigsky.notch.expressions;

import bigsky.notch.NotchElement;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.BetterList;
import bigsky.utils.chisel.Location;

import java.util.List;

public class NotchListLiteral extends NotchExpression {
    private List<NotchExpression> values;

    public NotchListLiteral(Location start, Location end) {
        super(start, end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        BetterList<Object> betterList = new BetterList<>();
        for (NotchExpression expression : values) {
            betterList.add(expression.evaluate(runtime));
        }
        return betterList;
    }

    public void setValues(List<NotchExpression> listValues) {
        this.values = addChildren(listValues);
    }

}
