package bigsky.notch;

public class ParseException extends RuntimeException {
    public final Location start, end;

    public ParseException(Location start, Location end, String message, Throwable cause) {
        super(message, cause);
        this.start = start;
        this.end = end;
    }

    public ParseException(Location start, Location end, String message) {
        super(message);
        this.start = start;
        this.end = end;
    }

    public ParseException(Location token, String message) {
        super(message);
        this.start = token;
        this.end = token;
    }
}
