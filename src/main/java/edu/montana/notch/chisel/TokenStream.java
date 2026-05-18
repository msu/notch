package edu.montana.notch.chisel;

import java.util.Collections;
import java.util.List;

public class TokenStream {
    protected Source source;
    protected List<Token> tokens;
    public int index = 0;

    public TokenStream(Source source, List<Token> tokens) {
        this.source = source;
        this.tokens = tokens;
    }

    public boolean atEnd() {
        return index >= tokens.size();
    }

    public Token peek() {
        if (index >= tokens.size()) return source.eoi;
        return tokens.get(index);
    }

    public Token prev() {
        if (index - 1 < 0) return source.soi;
        if (index - 1 >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(index - 1);
    }

    public Token take() {
        if (index >= tokens.size()) return source.eoi;
        if (index < 0) {
            index = 0;
            return source.soi;
        }
        var token = tokens.get(index);
        index += 1;
        return token;
    }

    public boolean match(Token token, String... types) {
        for (String type : types) {
            if (token.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean peek(String... types) {
        var token = peek();
        return match(token, types);
    }

    public boolean take(String... types) {
        var token = peek();
        if (match(token, types)) {
            take();
            return true;
        }
        return false;
    }

    public Token consume(String... types) {
        var token = peek();
        if (match(token, types)) {
            take();
            return token;
        }
        return null;
    }

    public List<Token> toList() {
        return Collections.unmodifiableList(tokens);
    }

    public Location location() {
        var token = peek();
        return token.span.start();
    }

    public Source getSource() {
        return source;
    }

    public void reset() {
        index = 0;
    }

    public CharSequence content() {
        return source.content;
    }

    public Lookahead lookahead() {
        return new Lookahead();
    }

    public int size() {
        return tokens.size();
    }

    public class Lookahead implements AutoCloseable {
        private final int startIndex;

        public Lookahead() {
            this.startIndex = index;
        }

        @Override
        public void close() {
            index = startIndex;
        }
    }
}