package bigsky.notch.json5;

import java.util.Objects;

public final class JSON5String extends JSON5Value {
    public final String value;

    public JSON5String(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof String s) return value.equals(s);
        if (!(o instanceof JSON5String that)) return false;
        return Objects.equals(value, that.value);
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
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (c < 0x20 || c > 0x7e) {
                        // Control characters need to be escaped as \\uXXXX
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
