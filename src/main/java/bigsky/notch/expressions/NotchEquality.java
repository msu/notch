package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.chisel.Token;

import java.util.Objects;

public class NotchEquality extends NotchExpression {
    public final NotchExpression lhs, rhs;
    private final Token op;

    public NotchEquality(Token op, NotchExpression lhs, NotchExpression rhs) {
        super(lhs.fileId, lhs.start, rhs.end);
        this.op = op;
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var lv = runtime.evaluate(lhs);
        var rv = runtime.evaluate(rhs);
        if (op.str().equals("==")) {
            return Objects.equals(lv, rv);
        } else {
            return !Objects.equals(lv, rv);
        }
    }
}
