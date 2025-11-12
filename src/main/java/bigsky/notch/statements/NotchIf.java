package bigsky.notch.statements;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

import java.util.List;
import java.util.Objects;

public class NotchIf extends NotchStatement {

    public final NotchExpression expr;
    public final List<NotchStatement> ifTrue;
    public final List<NotchStatement> ifFalse;

    public NotchIf(Location start, NotchExpression expr, List<NotchStatement> ifTrue, List<NotchStatement> ifFalse, Location end) {
        super(expr.fileId, start, end);
        this.expr = Objects.requireNonNull(expr);
        this.ifTrue = List.copyOf(ifTrue);
        this.ifFalse = List.copyOf(ifFalse);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object result = runtime.evaluate(expr);
        if (Boolean.TRUE.equals(result)) {
            for (NotchStatement stmt : ifTrue) {
                runtime.execute(stmt);
            }
        } else {
            for (NotchStatement stmt : ifFalse) {
                runtime.execute(stmt);
            }
        }
    }
}
