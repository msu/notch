package edu.montana.notch.chisel;

import edu.montana.notch.util.SafeAutoClosable;
import edu.montana.notch.util.Text;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BasicParser {
    protected TokenStream tokens;
    protected Set<TokenType> ignoredTokenTypes = new HashSet<>();
    protected Set<TokenType> temporaryEndTokens = new HashSet<>();

    public BasicParser(TokenStream tokens) {
        this.tokens = Objects.requireNonNull(tokens);
    }

    public Location location() {
        return tokens.location();
    }

    protected void consumeIgnoredTokens() {
        outer:
        do {
            if (_atEnd()) break;

            for (var tt : ignoredTokenTypes) {
                if (tokens.peek(tt)) {
                    tokens.take();
                    continue outer;
                }
            }

            break;
        } while (true);
    }

    boolean _atEnd() {
        for (var tt : temporaryEndTokens) {
            if (tokens.peek(tt)) {
                return true;
            }
        }
        return tokens.atEnd();
    }

    public boolean atEnd() {
        consumeIgnoredTokens();
        return _atEnd();
    }

    public Token peek() {
        consumeIgnoredTokens();
        if (atEnd()) return Token.EOF;
        return tokens.peek();
    }

    public boolean peek(TokenType... tokenTypes) {
        consumeIgnoredTokens();
        return tokens.peek(tokenTypes);
    }

    public boolean peek(TokenType tokenType) {
        consumeIgnoredTokens();
        if (atEnd()) return false;
        return tokens.peek(tokenType);
    }

    public Token take() {
        consumeIgnoredTokens();
        if (atEnd()) return null;
        return tokens.take();
    }

    public boolean take(TokenType tokenType) {
        if (!peek(tokenType)) return false;
        tokens.take();
        return true;
    }

    public Token consume(TokenType... tokenTypes) {
        if (peek(tokenTypes)) {
            return tokens.take();
        } else {
            return null;
        }
    }

    public Token require(TokenType tokenType, String message) {
        if (!peek(tokenType)) {
            throw new ParseException(fileId(), "%s: expected %s".formatted(message, Text.repr(tokenType)), location());
        }
        return take();
    }

    public String lex(Location start, Location end) {
        var subseq = tokens.source.subSequence(start.index, end.index);
        return subseq.toString();
    }

    public String fileId() {
        return tokens.getFileId();
    }

    // "pushes" the types onto the ignored type stack until this closable closes
    public IgnoredTypeGuard ignoreTypes(TokenType... types) {
        return new IgnoredTypeGuard(types);
    }

    public class IgnoredTypeGuard implements SafeAutoClosable {
        public final TokenType[] ignoredTypes;
        private final boolean[] preExistingTypes;

        public IgnoredTypeGuard(TokenType[] ignoredTypes) {
            this.ignoredTypes = ignoredTypes;
            this.preExistingTypes = new boolean[ignoredTypes.length];
            for (int i = 0; i < ignoredTypes.length; i++) {
                var type = ignoredTypes[i];
                preExistingTypes[i] = ignoredTokenTypes.contains(type);
            }
        }

        @Override
        public void close() {
            for (int i = 0; i < ignoredTypes.length; i++) {
                if (!preExistingTypes[i]) {
                    var type = ignoredTypes[i];
                    ignoredTokenTypes.remove(type);
                }
            }
        }
    }
}
