package bigsky.notch.json5;

import java.util.Objects;

public final class JSON5Boolean extends JSON5Value {
    public final boolean value;

    public static final JSON5Boolean TRUE = new JSON5Boolean(true);
    public static final JSON5Boolean FALSE = new JSON5Boolean(false);

    private JSON5Boolean(boolean value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Boolean b) return value == b;
        if (!(o instanceof JSON5Boolean that)) return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
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
