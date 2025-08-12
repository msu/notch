package bigsky.notch.expr;

import bigsky.notch.Location;
import bigsky.notch.runtime.NotchRuntime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class NotchExpression {
    public final Location start, end;
    private final List<NotchExpression> children = new LinkedList<>();

    public NotchExpression(Location start, Location end) {
        this.start = Objects.requireNonNull(start, "start location must not be null");
        this.end = Objects.requireNonNull(end, "end location must not be null");
    }

    protected <T extends NotchExpression> T addChild(T expr) {
        children.add(expr);
        return expr;
    }

    public abstract Object evaluate(NotchRuntime runtime);

    public List<NotchExpression> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void collectErrors(List<NotchErrorExpression> out) {
        if (this instanceof NotchErrorExpression bee) {
            out.add(bee);
        }
        for (var child : getChildren()) {
            child.collectErrors(out);
        }
    }
}
