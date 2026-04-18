package bigsky.notch.json5;

public final class JSON5Null extends JSON5Value {
    public static final JSON5Null NULL = new JSON5Null();

    private JSON5Null() {}

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
        out.append("null");
    }
}
