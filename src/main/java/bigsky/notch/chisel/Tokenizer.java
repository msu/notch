package bigsky.notch.chisel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static bigsky.notch.util.Text.repr;

public class Tokenizer {
    protected final String fileId;
    protected final CharSequence src;
    protected Location loc = new Location();
    protected List<TokenType> tokenTypes = new LinkedList<>();

    public Tokenizer() {
        fileId = null;
        src = null;
    }

    protected Tokenizer(String fileId, CharSequence src, Location location) {
        this.fileId = Objects.requireNonNull(fileId);
        this.src = Objects.requireNonNull(src);
        this.loc = location;
    }

    public Tokenizer create(String fileId, CharSequence src) {
        return create(fileId, src, new Location());
    }

    public Tokenizer create(String fileId, CharSequence src, Location start) {
        var out = new Tokenizer(fileId, src, start);
        out.tokenTypes.addAll(tokenTypes);
        return out;
    }

    public Tokenizer withTokenType(TokenType type) {
        Objects.requireNonNull(type);
        tokenTypes.add(type);
        return this;
    }

    public Tokenizer withTokenTypes(TokenType... types) {
        for (TokenType type : types) {
            withTokenType(type);
        }
        return this;
    }

    private Token peekedToken;

    public Token nextToken() throws TokenizeException {
        if (peekedToken != null) {
            var out = peekedToken;
            loc = out.end;
            peekedToken = null;
            return out;
        }

        var start = loc;
        for (var tt : tokenTypes) {
            var token = tt.tokenize(this);
            if (token != null) {
                return token;
            } else {
                loc = start;
            }
        }
        return null;
    }

    public Token peekToken() throws TokenizeException {
        if (peekedToken != null) {
            return peekedToken;
        }

        var start = loc;
        for (var tt : tokenTypes) {
            var token = tt.tokenize(this);
            loc = start;
            if (token != null) {
                peekedToken = token;
                return token;
            }
        }
        return null;
    }

    public TokenStream tokenize(String fileId, CharSequence src, Location start, TokenType... terminalTypes) throws TokenizeException {
        var tokenizer = create(fileId, src, start);
        return tokenizer.tokenize(terminalTypes);
    }

    public TokenStream tokenize(String fileId, CharSequence src, TokenType... terminalTypes) throws TokenizeException {
        var tokenizer = create(fileId, src);
        return tokenizer.tokenize(terminalTypes);
    }

    public TokenStream tokenize(TokenType... terminalTokenTypes) throws TokenizeException {
        if (src == null) {
            throw new TokenizeException(Location.SOF, "this tokenizer is a template, it cannot tokenize things");
        }

        var out = new ArrayList<Token>();

        var start = location();
        outer:
        while (start.index < src.length()) {
            var token = peekToken();
            if (token == null) {
                throw new TokenizeException(start, "expected token, found " + repr(peek()));
            }

            for (TokenType terminalType : terminalTokenTypes) {
                if (token.type.equals(terminalType)) {
                    break outer;
                }
            }

            token = nextToken(); // it was peeked so we can just cache this, we should always get the same token
            out.add(token);

            var loc = location();
            if (start.index == loc.index) {
                throw new TokenizeException(start, "infinite loop detected!");
            }
            start = loc;
        }

        return new TokenStream(fileId, src, out);
    }

    public Location location() {
        return loc;
    }

    public void location(Location loc) {
        this.loc = loc;
    }

    public boolean atEnd() {
        return loc.index >= src.length();
    }

    public char peek() {
        if (atEnd()) return 0;
        return src.charAt(loc.index);
    }

    public boolean peek(char... cs) {
        char pc = peek();
        for (var c : cs) {
            if (pc == c) return true;
        }
        return false;
    }

    public boolean peek(CharSequence cs) {
        if (loc.index + cs.length() > src.length()) return false;
        for (int i = 0; i < cs.length(); ) {
            char c = src.charAt(loc.index + i);
            if (c != cs.charAt(i)) {
                return false;
            }
            i += Character.charCount(c);
        }
        return true;
    }

    public char take() {
        if (atEnd()) return 0;
        var c = src.charAt(loc.index);
        loc = loc.next(c);
        return c;
    }

    public boolean take(char c) {
        char d = peek();
        if (d == c) {
            take();
            return true;
        }
        return false;
    }

    public boolean take(char... cs) {
        char d = peek();
        for (var c : cs) {
            if (d == c) {
                take();
                return true;
            }
        }
        return false;
    }

    public void take(int n) {
        int start = loc.index;
        while (loc.index < start + n) {
            take();
        }
    }

    public boolean take(CharSequence cs) {
        if (loc.index + cs.length() > src.length()) return false;
        Location end = loc;
        int i = 0;
        while (i < cs.length()) {
            char c = cs.charAt(i);
            if (src.charAt(loc.index + i) != c) return false;
            i += Character.charCount(c);
            end = end.next(c);
        }
        loc = end;
        return true;
    }

    public String seek(char end) {
        var content = new StringBuilder();
        while (!atEnd()) {
            var c = take();
            if (c == end) break;
            content.append(c);
        }
        return content.toString();
    }

    public String trySeek(String end) {
        var content = new StringBuilder();
        while (!atEnd()) {
            if (take(end)) return content.toString();
            content.append(take());
        }
        return null;
    }

    public String seek(String end) {
        var content = new StringBuilder();
        while (!atEnd()) {
            if (take(end)) break;
            content.append(take());
        }
        return content.toString();
    }

    public CharSequence source() {
        return src;
    }

    public String fileId() {
        return fileId;
    }

    @Override
    public String toString() {
        String str = String.valueOf(src);
        return str.substring(0, this.loc.index) + "*" + str.substring(this.loc.index);
    }

    public String lex(Location start, Location end) {
        return src.subSequence(start.index, end.index).toString();
    }
}
