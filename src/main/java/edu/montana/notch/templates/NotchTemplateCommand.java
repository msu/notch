package edu.montana.notch.templates;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.ast.content.NotchTemplateContentCommand;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class NotchTemplateCommand implements Spanned {
    public final String commandName;
    protected Span span;
    protected Token commandToken;
    protected List<NotchTemplateCommand> childCommands = new ArrayList<>();
    protected boolean isGlobal = false;
    protected boolean isSingleton = false;

    public NotchTemplateCommand(String commandName) {
        Objects.requireNonNull(commandName);
        this.commandName = commandName.toLowerCase();
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

    public abstract void parseCommand(NotchParser parser);

    public void parseBody(NotchTemplateParser parser) {
    }

    public void preRender(NotchTemplateRuntime runtime) {
    }

    public abstract void render(NotchTemplateRuntime runtime, Drain out);

    public void postRender(NotchTemplateRuntime runtime) {
    }

    protected void addChildCommand(NotchTemplateCommand cmd) {
        if (cmd != null) {
            childCommands.add(cmd);
        }
    }

    protected void addChildContent(NotchTemplateContentBlock content) {
        if (content == null) return;
        for (var item : content.content()) {
            if (item instanceof NotchTemplateContentCommand cmd) {
                addChildCommand(cmd.command);
            }
        }
    }

    public List<NotchTemplateCommand> getChildCommands() {
        return Collections.unmodifiableList(childCommands);
    }

    @Override
    public Span span() {
        return span;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public boolean isSingleton() {
        return isGlobal && isSingleton;
    }
}
