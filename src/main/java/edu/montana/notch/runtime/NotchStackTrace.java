package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.util.BetterList;

import java.util.Objects;

public class NotchStackTrace {
    public NotchStackTraceElement[] elements;
    public StackTraceElement[] fullTrace;

    public NotchStackTrace(NotchStackTraceElement[] elements, StackTraceElement[] fullTrace) {
        this.elements = elements;
        this.fullTrace = fullTrace;
    }

    public void render(StringBuilder sb, boolean showFullTrace) {
        sb.append("Notch Stack Trace:\n");
        sb.append("------------------\n");
        for (var elt : elements) {
            sb.append("    at ").append(elt.report()).append('\n');
        }
        if (showFullTrace) {
            sb.append("\n\nORIGINAL FULL TRACE:");
            for (var elt : fullTrace) {
                sb.append("- ").append(elt).append('\n');
            }
        }
    }

    public String render() {
        return render(false);
    }

    public String render(boolean showFullTrace) {
        var out = new StringBuilder();
        render(out, showFullTrace);
        return out.toString();
    }

    public NotchStackTrace localize(StackTraceElement reference) {
        var out = new BetterList<NotchStackTraceElement>();
        for (var elt : elements) {
            if (elt instanceof NotchStackTraceElement.Java j) {
                if (j.javaTraceElement.equals(reference)) break;
            }
            out.add(elt);
        }

        if (out.size() == elements.length) {
            throw new IllegalArgumentException("invalid reference passed, %s is not shared in this stack trace".formatted(reference));
        }

        return new NotchStackTrace(out.toArray(NotchStackTraceElement[]::new), fullTrace.clone());
    }

    public NotchStackTrace localize() {
        var trace = Thread.currentThread().getStackTrace();
        return localize(trace[3]);
    }

    @Override
    public String toString() {
        return render();
    }
}