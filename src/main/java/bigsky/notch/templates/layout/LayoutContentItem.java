package bigsky.notch.templates.layout;

import bigsky.notch.templates.runtime.NotchTemplateRuntime;

public interface LayoutContentItem {
    void render(NotchTemplateRuntime runtime, StringBuilder out);
}
