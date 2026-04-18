package bigsky.notch.json5;

public abstract sealed class JSON5Number extends JSON5Value permits JSON5Integer, JSON5Decimal {
    public abstract Number value();
}
