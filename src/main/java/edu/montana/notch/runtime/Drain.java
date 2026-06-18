package edu.montana.notch.runtime;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class Drain {
    private final Appendable out;

    public Drain(Appendable out) {
        this.out = out;
    }

    public Drain append(CharSequence cs) {
        if (cs == null) return this;
        try {
            out.append(cs);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public Drain append(char c) {
        try {
            out.append(c);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public Drain append(Object o) {
        return append(String.valueOf(o));
    }

    public Appendable raw() {
        return out;
    }
}
