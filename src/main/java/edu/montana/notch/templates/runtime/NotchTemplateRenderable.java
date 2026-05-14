package edu.montana.notch.templates.runtime;

import edu.montana.notch.runtime.NotchRenderable;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.List;

public interface NotchTemplateRenderable extends NotchRenderable {
    void render(List<Object> args, NotchTemplateRuntime runtime, StringBuilder sb);

    @Override
    default void render(List<Object> args, NotchRuntime runtime, StringBuilder sb) {
        render(args, (NotchTemplateRuntime) runtime, sb);
    }
}
