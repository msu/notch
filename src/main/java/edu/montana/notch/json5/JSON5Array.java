package edu.montana.notch.json5;

import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.JSON5;

import java.util.Objects;

public final class JSON5Array extends JSON5Value {
    public BetterList<JSON5Value> values;

    public JSON5Array() {
        values = new BetterList();
    }

    public JSON5Array(Iterable<?> iterable) {
        values = new BetterList<>();
        for (var item : iterable) {
            var val = JSON5.valueOf(item);
            values.add(val);
        }
    }

    public JSON5Array(Object[] values) {
        this.values = new BetterList<>();
        for (Object value : values) {
            var val = JSON5.valueOf(value);
            this.values.add(val);
        }
    }

    public void add(Object value) {
        values.add(JSON5.valueOf(value));
    }

    @Override
    public JSON5Array copy() {
        var out = new JSON5Array();
        out.values.addAll(values);
        return out;
    }

    @Override
    public JSON5Array deepCopy() {
        var out = new JSON5Array();
        for (JSON5Value value : out.values) {
            var newValue = value.deepCopy();
            out.values.add(newValue);
        }
        return out;
    }

    @Override
    public void encode(StringBuilder out, int indent, String newline) {
        out.append('[');
        int i = 0;
        for (JSON5Value value : values) {
            if (i++ > 0) out.append(',');
            else out.append(newline);
            value.encode(out, indent, newline);
            out.append(newline).append(" ".repeat(indent));
        }
        out.append(']');
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JSON5Array jsonArray)) return false;
        return Objects.equals(values, jsonArray.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }

    public int size() {
        return values.size();
    }

    public JSON5Value get(int i) {
        return values.get(i);
    }
}
