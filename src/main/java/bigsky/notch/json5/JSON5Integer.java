package bigsky.notch.json5;

import java.util.Objects;

public final class JSON5Integer extends JSON5Number {
    public final long value;

    public JSON5Integer(long value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Number n) return value == n.longValue();
        if (!(o instanceof JSON5Integer that)) return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public Number value() {
        return value;
    }

    @Override
    public JSON5Value copy() {
        return this;
    }

    @Override
    public JSON5Value deepCopy() {
        return this;
    }

    @Override
    public void encode(StringBuilder out, int indent, String newline) {
        out.append(value);
    }
}
