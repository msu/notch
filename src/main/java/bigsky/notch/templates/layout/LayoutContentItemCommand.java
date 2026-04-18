package bigsky.notch.templates.layout;

import bigsky.notch.templates.command.ContentCommand;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;

import java.util.Objects;

public record LayoutContentItemCommand(ContentCommand cmd) implements LayoutContentItem {
    public LayoutContentItemCommand {
        Objects.requireNonNull(cmd);
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder out) {
        cmd.blockContent().render(runtime, out);
    }
}
