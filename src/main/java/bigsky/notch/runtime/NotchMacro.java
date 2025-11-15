package bigsky.notch.runtime;

import java.util.List;

public interface NotchMacro {
    String getName();
    String getQualifiedName();

    void render(List<Object> args, NotchRuntime runtime, StringBuilder sb);
}
