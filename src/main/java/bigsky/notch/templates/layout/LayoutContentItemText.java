package bigsky.notch.templates.layout;

import bigsky.notch.templates.runtime.NotchTemplateRuntime;

public record LayoutContentItemText(String content) implements LayoutContentItem {
    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder out) {
        out.append(content);
    }
}
