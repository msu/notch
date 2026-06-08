package edu.montana.notch.runtime;

import edu.montana.notch.util.BetterList;

public interface NotchRenderable {
    String getName();

    String getQualifiedName();

    void render(BetterList<Object> args, NotchRuntime runtime, Drain out);
}
