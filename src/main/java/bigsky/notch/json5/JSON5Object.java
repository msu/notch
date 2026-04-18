package bigsky.notch.json5;

import bigsky.notch.util.JSON5;
import bigsky.notch.util.BetterMap;

import java.util.Map;
import java.util.Objects;

public final class JSON5Object extends JSON5Value {
    public final BetterMap<String, JSON5Value> values;

    public JSON5Object() {
        values = new BetterMap<>();
    }

    public JSON5Object(Map<?, ?> values) {
        this.values = new BetterMap<>();
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            var value = JSON5.valueOf(entry.getValue());
            this.values.put((String) entry.getKey(), value);
        }
    }

    public JSON5Object(Object... keyValues) {
        assert keyValues.length % 2 == 0;
        this.values = new BetterMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            var key = keyValues[i];
            var value = JSON5.valueOf(keyValues[i + 1]);
            this.values.put((String) key, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JSON5Object that)) return false;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }

    public void put(String name, Object value) {
        var val = JSON5.valueOf(value);
        values.put(name, val);
    }

    public JSON5Value get(String name) {
        return values.get(name);
    }

    public JSON5Value remove(String name) {
        return values.remove(name);
    }

    public int size() {
        return values.size();
    }

    @Override
    public JSON5Object copy() {
        var out = new JSON5Object();
        out.values.putAll(values);
        return out;
    }

    @Override
    public JSON5Object deepCopy() {
        var out = new JSON5Object();
        for (Map.Entry<String, JSON5Value> entry : this.values.entrySet()) {
            var value = entry.getValue().deepCopy();
            out.put(entry.getKey(), value);
        }
        return out;
    }

    @Override
    public void encode(StringBuilder out, int indent, String newline) {
        out.append('{');
        int i = 0;
        for (Map.Entry<String, JSON5Value> entry : values.entrySet()) {
            if (i++ > 0) out.append(',');
            else out.append(newline);
            new JSON5String(entry.getKey()).encode(out, indent, newline);
            out.append(':');
            entry.getValue().encode(out, indent, newline);
            out.append(newline).append(" ".repeat(indent));
        }
        out.append('}');
    }
}
