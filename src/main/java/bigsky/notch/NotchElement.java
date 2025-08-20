package bigsky.notch;

import bigsky.notch.expr.NotchErrorExpression;
import bigsky.notch.expr.NotchExpression;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class NotchElement {
    public final Location start, end;
    private final List<NotchElement> children = new LinkedList<>();

    public NotchElement(Location start, Location end) {
        this.start = Objects.requireNonNull(start, "start location must not be null");
        this.end = Objects.requireNonNull(end, "end location must not be null");
    }

    protected <T extends NotchElement> T addChild(T child) {
        children.add(Objects.requireNonNull(child, "child must not be null"));
        return child;
    }

    public List<NotchElement> getChildren() {
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
