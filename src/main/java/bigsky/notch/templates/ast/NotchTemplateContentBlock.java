package bigsky.notch.templates.ast;

import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.ast.content.NotchTemplateContentCommand;
import bigsky.notch.templates.ast.content.NotchTemplateContentExpression;
import bigsky.notch.templates.ast.content.NotchTemplateContentItem;
import bigsky.notch.templates.ast.content.NotchTemplateContentText;
import bigsky.notch.templates.runtime.RenderException;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.util.BetterList;
import bigsky.notch.util.Exceptions;

import java.util.*;

import static bigsky.notch.util.Text.repr;

public class NotchTemplateContentBlock {
    private final List<NotchTemplateContentItem> contentItems;

    public NotchTemplateContentBlock(List<NotchTemplateContentItem> contentItems) {
        this.contentItems = contentItems;
    }

    public void render(NotchTemplateRuntime runtime, StringBuilder out) {
        var errors = new ArrayList<RenderException>();
        for (var item : items()) {
            try {
                if (Objects.requireNonNull(item) instanceof NotchTemplateContentText itemText) {
                    out.append(itemText.content);
                } else if (item instanceof NotchTemplateContentCommand itemCmd) {
                    var cmd = itemCmd.command;
                    runtime.render(cmd, out);
                } else if (item instanceof NotchTemplateContentExpression itemExpr) {
                    runtime.render(itemExpr.expression, out);
                } else {
                    throw new UnsupportedOperationException("don't know how to compile " + item.getClass());
                }
            } catch (RenderException e) {
                errors.add(e);
            }
        }

        if (!errors.isEmpty()) {
            // TODO: throw many errors
            throw Exceptions.rethrow(errors.get(0));
        }
    }

    public List<NotchTemplateContentItem> items() {
        return Collections.unmodifiableList(contentItems);
    }

    public NotchTemplateCommand lastCommand() {
        if (contentItems.isEmpty()) return null;
        var item = contentItems.get(contentItems.size() - 1);
        if (item instanceof NotchTemplateContentCommand cmd) {
            return cmd.command;
        }
        return null;
    }

    public GlobalCommands collectGlobalCommands() {
        var out = new GlobalCommands();
        for (var item : items()) {
            if (item instanceof NotchTemplateContentCommand itemCmd) {
                collectGlobalCommands(out, itemCmd.command);
            }
        }
        return out;
    }

    private void collectGlobalCommands(GlobalCommands out, NotchTemplateCommand command) {
        if (command instanceof NotchTemplateCommand.Global global) {
            out.globals.add(global);

            if (command instanceof NotchTemplateCommand.Singleton singleton) {
                var cmdName = singleton.getCommand().name;
                if (out.singletons.containsKey(cmdName)) {
                    throw new IllegalStateException("multiple singletons for " + repr(cmdName) + " defined");
                }
                out.singletons.put(cmdName, singleton);
            }
        }

        for (var child : command.getChildCommands()) {
            collectGlobalCommands(out, child);
        }
    }

    public record GlobalCommands(
        BetterList<NotchTemplateCommand.Global> globals,
        Map<String, NotchTemplateCommand.Singleton> singletons
    ) {
        public GlobalCommands() {
            this(new BetterList<>(), new LinkedHashMap<>());
        }

        public <T extends NotchTemplateCommand.Global> BetterList<T> globals(Class<T> clazz) {
            var out = new BetterList<T>();
            for (var global : globals) {
                if (clazz.isInstance(global)) {
                    out.add(clazz.cast(global));
                }
            }
            return out;
        }
    }
}
