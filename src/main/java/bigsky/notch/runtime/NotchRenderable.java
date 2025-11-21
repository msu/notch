package bigsky.notch.runtime;

import java.util.List;

public interface NotchRenderable {
    String getName();
    String getQualifiedName();

    void render(List<Object> args, NotchRuntime runtime, StringBuilder sb);
}
