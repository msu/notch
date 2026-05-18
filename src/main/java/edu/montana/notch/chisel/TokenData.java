package bigsky.notch.chisel;

public record TokenData(String tokenType, Object value) {
    public TokenData(Object value) {
        this(null, value);
    }

    public TokenData() {
        this(null, null);
    }

    public String str() {
        return (String) value;
    }

    public int integer() {
        return (int) (Integer) value;
    }
}
