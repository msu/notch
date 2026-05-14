package edu.montana.notch.templates;

import edu.montana.notch.NotchParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.ast.content.NotchTemplateContentCommand;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.chisel.Token;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class NotchTemplateCommand implements Spanned {
    public final String name;
    protected Location start, end;
    protected String fileId;
    protected List<NotchTemplateCommand> childCommands = new ArrayList<>();

    public NotchTemplateCommand(String name) {
        Objects.requireNonNull(name);
        this.name = name.toLowerCase();
    }

    public NotchTemplateCommand newInstance() {
        var clazz = getClass();
        try {
            var cons = clazz.getConstructor();
            return cons.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser);

    public abstract void render(NotchTemplateRuntime runtime, StringBuilder sb);

    protected void addChildCommand(NotchTemplateCommand cmd) {
        if (cmd != null) {
            childCommands.add(cmd);
        }
    }

    protected void addChildContent(NotchTemplateContentBlock content) {
        if (content == null) return;
        for (var item : content.items()) {
            if (item instanceof NotchTemplateContentCommand cmd) {
                addChildCommand(cmd.command);
            }
        }
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    public List<NotchTemplateCommand> getChildCommands() {
        return Collections.unmodifiableList(childCommands);
    }

    public abstract static class ContextCommand {
        public final String name;

        public ContextCommand(String name) {
            Objects.requireNonNull(name);
            this.name = name.toLowerCase();
        }

        public abstract void parse(NotchTemplateParser parser);
    }

    public interface Global {
        default NotchTemplateCommand getCommand() {
            return (NotchTemplateCommand) this;
        }

        default void preRender(NotchTemplateRuntime runtime) {
        }

        default void postRender(NotchTemplateRuntime runtime, StringBuilder sb) {
        }
    }

    public interface Singleton extends Global {
    }

    @Override
    public Span span() {
        return new Span(start, end);
    }
}
