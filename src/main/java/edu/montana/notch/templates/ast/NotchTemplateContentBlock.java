package edu.montana.notch.templates.ast;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.ast.content.NotchTemplateContentCommand;
import edu.montana.notch.templates.ast.content.NotchTemplateContentExpression;
import edu.montana.notch.templates.ast.content.NotchTemplateContentItem;
import edu.montana.notch.templates.ast.content.NotchTemplateContentText;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.templates.runtime.RenderException;
import edu.montana.notch.util.BetterList;
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

    public String globalRender(NotchTemplateRuntime runtime) {
        final var out = new StringBuilder();
        globalRender(runtime, out);
        return out.toString();
    }

    public void globalRender(NotchTemplateRuntime runtime, StringBuilder out) {
        final var globals = collectGlobals();
        try {
            for (var global : globals) {
                global.preRender(runtime);
            }
        } catch (RenderException e) {
            throw Exceptions.rethrow(e);
        } catch (Exception e) {
            throw new RenderException(e);
        }

        render(runtime, out);

        try {
            for (var global : globals) {
                global.postRender(runtime);
            }
        } catch (RenderException e) {
            throw Exceptions.rethrow(e);
        } catch (Exception e) {
            throw new RenderException(e);
        }
    }

    public void render(NotchTemplateRuntime runtime, StringBuilder out) {
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

    public BetterList<NotchTemplateCommand> collectGlobals() {
        final var out = new BetterList<NotchTemplateCommand>();
        collectGlobals(out);
        return out;
    }

    public void collectGlobals(BetterList<NotchTemplateCommand> out) {
        for (var item : content()) {
            if (item instanceof NotchTemplateContentCommand cmd) {
                collectGlobals(out, cmd.command);
            }
        }
        var cmd = terminalCommand();
        if (cmd != null) {
            collectGlobals(out, cmd);
        }
    }

    private void collectGlobals(BetterList<NotchTemplateCommand> out, NotchTemplateCommand command) {
        if (command.isGlobal()) {
            out.add(command);
        }

        for (final var child : command.getChildCommands()) {
            collectGlobals(out, child);
        }
    }

    public <T extends NotchTemplateCommand> BetterList<T> collectGlobals(Class<T> clazz) {
        final var out = new BetterList<T>();
        collectGlobals(out, clazz);
        return out;
    }


    public <T extends NotchTemplateCommand> void collectGlobals(BetterList<T> out, Class<T> clazz) {
        for (var item : content()) {
            if (!(item instanceof NotchTemplateContentCommand cmd)) {
                continue;
            }
            collectGlobals(out, clazz, cmd.command);
        }
    }

    private <T extends NotchTemplateCommand> void collectGlobals(BetterList<T> out, Class<T> clazz, NotchTemplateCommand command) {
        if (command.isGlobal() && clazz.isAssignableFrom(command.getClass())) {
            out.add(clazz.cast(command));
        }

        for (final var child : command.getChildCommands()) {
            collectGlobals(out, clazz, child);
        }
    }
}
