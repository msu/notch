package edu.montana.notch.chisel;

import java.util.Objects;
import java.util.PrimitiveIterator;

public final class Location {
    public static final Location SOI = new Location(0, 0, 0);
    public static final Location EOI = new Location(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    public final int index, line, column;

    public Location(int index, int line, int column) {
        this.index = index;
        this.line = line;
        this.column = column;
    }

    public Location() {
        this(0, 1, 1);
    }

    public Location next(char c) {
        int i = index + Character.charCount(c);
        if (c == '\n') {
            return new Location(i, line + 1, 1);
        } else {
            return new Location(i, line, column + 1);
        }
    }

    public Location next(CharSequence cs) {
        var out = this;
        for (PrimitiveIterator.OfInt it = cs.chars().iterator(); it.hasNext(); ) {
            var c = (char) (int) it.next();
            out = out.next(c);
        }
        return out;
    }

    public Location prev(char c) {
        int i = index - Character.charCount(c);
        assert c != '\n';
        return new Location(i, line, column - 1);
    }

    public boolean isSentinel() {
        return this.equals(SOI) || this.equals(EOI);
    }

    public String display() {
        if (this.equals(EOI)) return "EOF";
        if (this.equals(SOI)) return "SOF";
        return "%d:%d (%d)".formatted(line, column, index);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Location location)) return false;
        return index == location.index && line == location.line && column == location.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, line, column);
    }

    @Override
    public String toString() {
        return "Loc(" + display() + ")";
    }
}
