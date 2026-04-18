package bigsky.notch.templates.runtime;

import bigsky.notch.runtime.NotchRenderable;
import bigsky.notch.runtime.NotchRuntime;

import java.util.List;

public interface NotchTemplateRenderable extends NotchRenderable {
    void render(List<Object> args, NotchTemplateRuntime runtime, StringBuilder sb);

    @Override
    default void render(List<Object> args, NotchRuntime runtime, StringBuilder sb) {
        render(args, (NotchTemplateRuntime) runtime, sb);
    }
}
