package edu.montana.notch.chisel;

import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.Pair;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static edu.montana.notch.util.Pair.pair;
import static edu.montana.notch.util.Text.repr;

public class Tokenizer {
    protected final Source source;
    protected Location loc;
    protected List<Pair<String, TokenType>> tokenTypes = new LinkedList<>();

    public Tokenizer() {
        source = null;
        loc = new Location();
    }

    protected Tokenizer(Source source, Location start) {
        this.source = source;
        this.loc = start;
    }

    public Tokenizer create(Source source) {
        return create(source, new Location());
    }

    public Tokenizer create(Source source, Location start) {
        var out = new Tokenizer(source, start);
        out.tokenTypes.addAll(tokenTypes);
        return out;
    }

    public Tokenizer withTokenType(String name, TokenType type) {
        Objects.requireNonNull(type);
        tokenTypes.add(pair(name, type));
        return this;
    }

    public Tokenizer withTokenTypes(String name, TokenType... types) {
        for (TokenType type : types) {
            withTokenType(name, type);
        }
        return this;
    }

    public Tokenizer withTokenTypes(Pair<String, TokenType>... types) {
        tokenTypes.addAll(Arrays.asList(types));
        return this;
    }

    private Token peekedToken;

    public Token nextToken() throws TokenizeException {
        if (peekedToken != null) {
            var out = peekedToken;
            peekedToken = null;
            loc = out.end();
            return out;
        }

        Location start;
        for (var tt : tokenTypes) {
            start = loc;

            var tokenData = tt.second().tokenize(this);
            if (tokenData == null) {
                loc = start;
                continue;
            }

            Token token = createToken(tt, start, tokenData);
            return token;
        }
        return null;
    }

    public Token peekToken() throws TokenizeException {
        if (peekedToken != null) {
            return peekedToken;
        }

        Location start;
        for (var tt : tokenTypes) {
            start = loc;

            var tokenData = tt.second().tokenize(this);
            if (tokenData == null) {
                loc = start;
                continue;
            }

            peekedToken = createToken(tt, start, tokenData);
            loc = start;
            return peekedToken;
        }
        return null;
    }

    private Token createToken(Pair<String, TokenType> tt, Location start, TokenData data) {
        Objects.requireNonNull(data, "cannot create a token with null data");
        String tokenType = data.tokenType();
        if (tokenType == null) tokenType = tt.first();
        final var span = new Span(source, start, loc);
        final var token = new Token(span, tokenType, data.value());
        return token;
    }

    public TokenStream tokenize(Source source, Location start, String... terminalTypes) throws TokenizeException {
        var tokenizer = create(source, start);
        return tokenizer.tokenize(terminalTypes);
    }

    public TokenStream tokenize(Source source, String... terminalTypes) throws TokenizeException {
        var tokenizer = create(source);
        return tokenizer.tokenize(terminalTypes);
    }

    public TokenStream tokenize(String... terminalTokenTypes) throws TokenizeException {
        if (source == null) {
            final var diag = new Diagnostic();
            diag.highlight(currentCharSpan());
            diag.note("this is a template tokenizer, it cannot tokenize things");
            throw new TokenizeException(diag);
        }

        var out = new BetterList<Token>();

        var start = currentCharSpan();
        while (start.start().index < source.content.length()) {
            var token = tokenizeOne(terminalTokenTypes);
            if (token == null) break;
            out.add(token);

            var loc = currentCharSpan();
            if (start.start().index == loc.start().index) {
                final var diag = new Diagnostic();
                diag.note("infinite loop detected");
                diag.note("found a %s token here, but did not advance the input".formatted(repr(token.type)));
                diag.note("here's the associated token data: %s".formatted(repr(token.data)));
                throw new TokenizeException(diag, "infinite loop");
            }
            start = loc;
        }

        return new TokenStream(source, out);
    }

    public Token tokenizeOne(String... terminalTokenTypes) throws TokenizeException {
        var token = peekToken();
        if (token == null) {
            final var diag = new Diagnostic();
            diag.highlight(currentCharSpan());
            diag.note("unexpected character " + repr(peek()));
            throw new TokenizeException(diag);
        }

        for (String terminalType : terminalTokenTypes) {
            if (token.type.equals(terminalType)) {
                return null;
            }
        }

        token = nextToken(); // it was peeked so we can just cache this, we should always get the same token
        return token;
    }

    public Span currentCharSpan() {
        return new Span(source, loc, loc);
    }

    public Location location() {
        return loc;
    }

    public void location(Location loc) {
        this.loc = loc;
    }

    public boolean atEnd() {
        return loc.index >= content().length();
    }

    public char peek() {
        if (atEnd()) return 0;
        return content().charAt(loc.index);
    }

    public boolean peek(char... cs) {
        char pc = peek();
        for (var c : cs) {
            if (pc == c) return true;
        }
        return false;
    }

    public boolean peek(CharSequence cs) {
        if (loc.index + cs.length() > content().length()) return false;
        for (int i = 0; i < cs.length(); ) {
            char c = content().charAt(loc.index + i);
            if (c != cs.charAt(i)) {
                return false;
            }
            i += Character.charCount(c);
        }
        return true;
    }

    public char take() {
        if (atEnd()) return 0;
        var c = content().charAt(loc.index);
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
        if (loc.index + cs.length() > content().length()) return false;
        Location end = loc;
        int i = 0;
        while (i < cs.length()) {
            char c = cs.charAt(i);
            if (content().charAt(loc.index + i) != c) return false;
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

    /// returns null if end is not found
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

    public Source source() {
        if (source == null) {
            throw new IllegalStateException("this is a template tokenizer, it has not be instantiated with a source yet, please call .create!");
        }
        return source;
    }

    public CharSequence content() {
        return source().content;
    }

    @Override
    public String toString() {
        String str = String.valueOf(content());
        return str.substring(0, this.loc.index) + "*" + str.substring(this.loc.index);
    }

    public String lex(Location start, Location end) {
        return source().content.subSequence(start.index, end.index).toString();
    }
}
