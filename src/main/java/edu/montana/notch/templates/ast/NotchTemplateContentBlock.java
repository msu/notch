package edu.montana.notch.templates.ast;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.ast.content.NotchTemplateContentCommand;
import edu.montana.notch.templates.ast.content.NotchTemplateContentExpression;
import edu.montana.notch.templates.ast.content.NotchTemplateContentItem;
import edu.montana.notch.templates.ast.content.NotchTemplateContentText;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.templates.runtime.RenderException;
import edu.montana.notch.util.Exceptions;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NotchTemplateContentBlock implements Spanned {
    private final Span span;
    private final List<NotchTemplateContentItem> content;
    private final NotchTemplateCommand terminalCommand;

    public NotchTemplateContentBlock(Span span, List<NotchTemplateContentItem> content, NotchTemplateCommand terminalCommand) {
        this.span = span;
        this.content = content;
        this.terminalCommand = terminalCommand;
    }

    public String fullRender(NotchTemplateRuntime runtime) {
        final var sb = new StringBuilder();
        fullRender(runtime, new Drain(sb));
        return sb.toString();
    }

    public void fullRender(NotchTemplateRuntime runtime, Drain out) {
        try {
            preRender(runtime);
        } catch (RenderException e) {
            throw Exceptions.rethrow(e);
        } catch (Exception e) {
            throw new RenderException(e);
        }

        render(runtime, out);

        try {
            postRender(runtime);
        } catch (RenderException e) {
            throw Exceptions.rethrow(e);
        } catch (Exception e) {
            throw new RenderException(e);
        }
    }

    public void render(NotchTemplateRuntime runtime, Drain out) {
        for (var item : content()) {
            try {
                if (Objects.requireNonNull(item) instanceof NotchTemplateContentText itemText) {
                    out.append(itemText.content);
                } else if (item instanceof NotchTemplateContentCommand itemCmd) {
                    var cmd = itemCmd.command;
                    runtime.render(cmd, out);
                } else if (item instanceof NotchTemplateContentExpression itemExpr) {
                    runtime.render(itemExpr.expression, out);
                } else {
                    throw new UnsupportedOperationException("unreachable: don't know how to render " + item.getClass());
                }
            } catch (RenderException e) {
                throw Exceptions.rethrow(e);
            } catch (Exception e) {
                throw new RenderException(e);
            }
        }
    }

    public List<NotchTemplateContentItem> content() {
        return Collections.unmodifiableList(content);
    }

    public NotchTemplateCommand terminalCommand() {
        return terminalCommand;
    }

    @Override
    public Span span() {
        return span;
    }

    public void preRender(NotchTemplateRuntime runtime) {
        for (var item : content()) {
            if (item instanceof NotchTemplateContentCommand itemCmd) {
                runtime.preRender(itemCmd.command);
            }
        }
        var terminalCommand = terminalCommand();
        if (terminalCommand != null) {
            terminalCommand.preRender(runtime);
        }
    }

    public void postRender(NotchTemplateRuntime runtime) {
        for (var item : content()) {
            if (item instanceof NotchTemplateContentCommand itemCmd) {
                itemCmd.command.postRender(runtime);
            }
        }
        var terminalCommand = terminalCommand();
        if (terminalCommand != null) {
            terminalCommand.postRender(runtime);
        }
    }
}
