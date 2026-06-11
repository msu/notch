package edu.montana.notch.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NotchInstance {
    public final NotchClass type;
    public final Map<String, Object> fields = new LinkedHashMap<>();

    public NotchInstance(NotchClass type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.name + fields;
    }
}
