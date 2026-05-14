package edu.montana.notch.json5;

import edu.montana.notch.json5.query.QueryEngine;

public abstract sealed class JSON5Value permits JSON5Object, JSON5Array, JSON5String, JSON5Number, JSON5Null, JSON5Boolean {
    public abstract JSON5Value copy();
    public abstract JSON5Value deepCopy();

    public Object query(String path) {
        return QueryEngine.query(this, path);
    }

    public <T> T query(String path, Class<T> clazz) {
        return QueryEngine.query(this, path, clazz);
    }

    public abstract void encode(StringBuilder out, int indent, String newline);

    public String encode() {
        var out = new StringBuilder();
        encode(out, 0, "");
        return out.toString();
    }

    public long longValue() {
        if (this instanceof JSON5Number n) {
            return n.value().longValue();
        }
        throw new IllegalStateException("json value is not a number");
    }

    public int intValue() {
        if (this instanceof JSON5Number n) {
            return n.value().intValue();
        }
        throw new IllegalStateException("json value is not a number");
    }

    public double doubleValue() {
        if (this instanceof JSON5Number n) {
            return n.value().doubleValue();
        }
        throw new IllegalStateException("json value is not a number");
    }

    public float floatValue() {
        if (this instanceof JSON5Number n) {
            return n.value().floatValue();
        }
        throw new IllegalStateException("json value is not a number");
    }

    public String stringValue() {
        if (this instanceof JSON5String s) {
            return s.value;
        }
        throw new IllegalStateException("json value is not a string");
    }

    public boolean booleanValue() {
        if (this instanceof JSON5Boolean b) {
            return b.value;
        }
        throw new IllegalStateException("json value is not a string");
    }

    public boolean has(String exp) {
        return QueryEngine.has(this, exp);
    }

    public boolean has(String exp, Class<?> clazz) {
        return QueryEngine.has(this, exp, clazz);
    }
}
