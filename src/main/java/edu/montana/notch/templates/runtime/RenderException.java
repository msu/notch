package edu.montana.notch.templates.runtime;

import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.util.BetterList;

import java.io.*;
import java.util.List;

public class RenderException extends RuntimeException {
    private final BetterList<Exception> children;

    public RenderException(String message) {
        super(message);
        children = new BetterList<>();
    }

    public RenderException(Exception e) {
        children = new BetterList<>();
        children.add(e);
    }

    public RenderException(BetterList<Exception> errors) {
        this.children = errors;
    }

    public String render() {
        var msg = new StringBuilder();
        for (final var ex : children) {
            if (ex instanceof ParseException e) {
                e.diagnostic.render(msg);
            } else if (ex instanceof TokenizeException e) {
                e.diagnostic.render(msg);
            } else if (ex instanceof NotchRuntimeException e) {
                e.diagnostic.render(msg);
            } else {
                msg.append("uncaught exception:\n");
                msg.append("- %s".formatted(ex));
                ex.printStackTrace(new PrintWriter(new OutputStream() {
                    @Override
                    public void write(int b) {
                        msg.append((char) b);
                    }
                }));
                msg.append('\n');
            }
        }

        String superMessage = super.getMessage();
        if (superMessage != null) {
            msg.append("\nInternal Error: ");
            msg.append(superMessage);
        }

        return msg.toString();
    }

    @Override
    public String getMessage() {
        return render();
    }

    public List<Exception> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return render();
    }
}
