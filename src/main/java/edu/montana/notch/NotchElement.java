package edu.montana.notch;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.expressions.NotchErrorExpression;
import edu.montana.notch.statements.NotchErrorStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class NotchElement implements Spanned {
    public final Span span;
    private final List<NotchElement> children = new LinkedList<>();

    public NotchElement(Span span) {
        this.span = span;
    }

    protected <T extends NotchElement> T addChild(T child) {
        if (child != null) children.add(child);
        return child;
    }

    protected <V extends NotchElement, T extends Collection<V>> T addChildren(T children) {
        if (children != null) {
            for (V child : children) {
                addChild(child);
            }
        }
        return children;
    }

    public List<NotchElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public List<Diagnostic> collectErrors() {
        List<Diagnostic> out = new ArrayList<>();
        collectErrors(out);
        return out;
    }

    private void collectErrors(List<Diagnostic> out) {
        if (this instanceof NotchErrorStatement s) {
            out.add(s.diagnostic);
        } else if (this instanceof NotchErrorExpression e) {
            out.add(e.diagnostic);
        }
        for (var child : getChildren()) {
            child.collectErrors(out);
        }
    }

    @Override
    public Span span() {
        return span;
    }
}
