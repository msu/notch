package edu.montana.notch.templates.runtime;

import edu.montana.notch.runtime.NotchRenderable;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.BetterList;

public interface NotchTemplateRenderable extends NotchRenderable {
    void render(BetterList<Object> args, NotchTemplateRuntime runtime, StringBuilder sb);

    @Override
    default void render(BetterList<Object> args, NotchRuntime runtime, StringBuilder sb) {
        render(args, (NotchTemplateRuntime) runtime, sb);
    }

    default String render(BetterList<Object> args, NotchRuntime runtime) {
        final var out = new StringBuilder();
        render(args, runtime, out);
        return out.toString();
    }
}
