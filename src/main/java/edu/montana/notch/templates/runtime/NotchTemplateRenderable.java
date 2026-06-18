package edu.montana.notch.templates.runtime;

import edu.montana.notch.runtime.Drain;
import edu.montana.notch.runtime.NotchRenderable;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.util.BetterList;

public interface NotchTemplateRenderable extends NotchRenderable {
    void render(BetterList<Object> args, NotchTemplateRuntime runtime, Drain out);

    @Override
    default void render(BetterList<Object> args, NotchRuntime runtime, Drain out) {
        render(args, (NotchTemplateRuntime) runtime, out);
    }

    default String render(BetterList<Object> args, NotchRuntime runtime) {
        final var sb = new StringBuilder();
        render(args, runtime, new Drain(sb));
        return sb.toString();
    }
}
