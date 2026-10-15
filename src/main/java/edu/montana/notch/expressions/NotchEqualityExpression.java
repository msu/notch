package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.Objects;

public class NotchEqualityExpression extends NotchExpression {
    public final NotchExpression lhs, rhs;
    private final Token op;

    public NotchEqualityExpression(Token op, NotchExpression lhs, NotchExpression rhs) {
        super(lhs.span.through(rhs));
        this.op = op;
        this.lhs = addChild(lhs);
        this.rhs = addChild(rhs);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        var lv = runtime.evaluate(lhs);
        var rv = runtime.evaluate(rhs);
        boolean q;
        if (lv instanceof String s && rv instanceof Character c) q = s.length() == 1 && s.charAt(0) == c;
        else if (lv instanceof Character c && rv instanceof String s) q = s.length() == 1 && s.charAt(0) == c;
        else q = Objects.equals(lv, rv);

        return op.str().equals("==") == q;
    }
}
