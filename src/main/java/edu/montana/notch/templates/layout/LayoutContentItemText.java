package edu.montana.notch.templates.layout;

import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

public record LayoutContentItemText(String content) implements LayoutContentItem {
    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder out) {
        out.append(content);
    }
}
