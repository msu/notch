package bigsky.notch;

public class NotchToken {
    public final Location start, end;
    public final String lex;
    public final String type;

    public NotchToken(Location start, Location end, String lex, String type) {
        this.start = start;
        this.end = end;
        this.lex = lex;
        this.type = type;
    }
}
