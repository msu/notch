package bigsky.notch;

public record Location(int index, int line, int column) {
    public static final Location SOF = new Location(0, 1, 1);
}
