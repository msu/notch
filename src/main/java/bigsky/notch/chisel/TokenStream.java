package bigsky.notch.chisel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TokenStream {
    protected String fileId;
    protected CharSequence source;
    protected List<Token> tokens;
    protected int index = 0;

    public TokenStream(String fileId, CharSequence source, List<Token> tokens) {
        this.fileId = Objects.requireNonNull(fileId);
        this.source = source;
        this.tokens = tokens;
    }

    public boolean atEnd() {
        return index >= tokens.size();
    }

    public Token peek() {
        if (index >= tokens.size()) return Token.EOF;
        return tokens.get(index);
    }

    public Token prev() {
        if (index - 1 < 0) return Token.SOF;
        if (index - 1 >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(index - 1);
    }

    public Token take() {
        if (index >= tokens.size()) return Token.EOF;
        if (index < 0) {
            index = 0;
            return Token.SOF;
        }
        var token = tokens.get(index);
        index += 1;
        return token;
    }

    public boolean match(Token token, TokenType... types) {
        for (TokenType type : types) {
            if (token.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean peek(TokenType... types) {
        var token = peek();
        return match(token, types);
    }

    public boolean take(TokenType... types) {
        var token = peek();
        if (match(token, types)) {
            take();
            return true;
        }
        return false;
    }

    public Token consume(TokenType... types) {
        var token = peek();
        if (match(token ,types)) {
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
        return token.start;
    }

    public String getFileId() {
        return fileId;
    }

    public void reset() {
        index = 0;
    }

    public CharSequence source() {
        return source;
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