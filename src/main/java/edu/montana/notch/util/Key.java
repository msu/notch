package edu.montana.notch.util;

/// A globally unique key with a label
public final class Key<T> {
    public final String label;

    public Key(String name) {
        this.label = name;
    }

    @Override
    public String toString() {
        return "Key(%s)".formatted(label);
    }
}
