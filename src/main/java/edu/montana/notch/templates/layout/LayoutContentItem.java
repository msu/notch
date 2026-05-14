package edu.montana.notch.templates.layout;

import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

public interface LayoutContentItem {
    void render(NotchTemplateRuntime runtime, StringBuilder out);
}
