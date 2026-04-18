package bigsky.notch.json5;

import java.util.Objects;

public final class JSON5Decimal extends JSON5Number {
    public final double value;

    public JSON5Decimal(double value) {
        this.value = value;
    }

    @Override
    public Number value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Double d) return value == d;
        if (!(o instanceof JSON5Decimal that)) return false;
        return Double.compare(value, that.value) == 0;
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
