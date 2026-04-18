package bigsky.notch.templates.runtime;

import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.notch.runtime.SourceProvider;
import bigsky.notch.util.Text;
import bigsky.notch.chisel.Location;
import bigsky.notch.chisel.ParseException;
import bigsky.notch.chisel.Span;
import bigsky.notch.chisel.Spanned;

import static bigsky.notch.chisel.Span.mkSpan;

public class RenderException extends RuntimeException implements Spanned {
    public final Span span;

    public RenderException(Span span, String message, Throwable t) {
        super(message, t);
        this.span = span;
    }

    public RenderException(Span span, Throwable throwable) {
        super(throwable);
        this.span = span;
    }

    public RenderException(Span span, String message) {
        super(message);
        this.span = span;
    }

    public RenderException(Location loc, String message) {
        this(mkSpan(loc), message);
    }

    public RenderException(Location loc, String message, Throwable cause) {
        this(mkSpan(loc), message, cause);
    }

    public RenderException(Location start, Location end, String message) {
        this(mkSpan(start, end), message);
    }

    public RenderException(Location start, Location end, Throwable cause) {
        this(mkSpan(start, end), cause);
    }

    public RenderException(Location start, Location end, String message, Throwable cause) {
        this(mkSpan(start, end), message, cause);
    }

    @Override
    public Span span() {
        return span;
    }

    public String render(SourceProvider sp) {
        var msg = new StringBuilder();
        if (getCause() instanceof NotchRuntimeException e) {
            e.diagnostic.render(sp, msg);

            msg.append('\n');
            msg.append("Trace\n");
            for (var elt : e.stackTrace.elements) {
                msg.append("- ")
                        .append(elt.hint())
                        .append(" (")
                        .append(elt.file())
                        .append(":")
                        .append(elt.span().start().line)
                        .append(")")
                        .append('\n');
            }
        } else if (getCause() instanceof ParseException e) {
            msg.append("Parse Exception!\n");
            msg.append("at ").append(e.span()).append('\n');
            msg.append(e.getMessage());
        }
        return msg.toString();
    }

    @Override
    public String toString() {
        var msg = new StringBuilder();
        if (getCause() instanceof NotchRuntimeException e) {
            msg.append("\n").append(e.getMessage()).append('\n');
            for (var elt : e.stackTrace.elements) {
                msg.append("- ")
                        .append(elt.file())
                        .append(" ")
                        .append(elt.span().start().line)
                        .append(":")
                        .append(elt.span().start().column)
                        .append('\n');
            }
        }

        Throwable t = this;
        do {
            if (t instanceof Spanned spanned) {
                msg.append("  at %s | %s - %s\n".formatted(spanned.span().start().display(), Text.simpleClassName(t), t.getMessage()));
            } else {
                msg.append("      %s - %s\n".formatted(Text.simpleClassName(t), t.getMessage()));
                break;
            }
            t = t.getCause();
        } while (t != null);
        return msg.toString();
    }
}
