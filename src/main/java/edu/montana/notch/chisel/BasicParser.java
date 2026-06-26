package edu.montana.notch.chisel;

import edu.montana.notch.util.SafeAutoClosable;
import edu.montana.notch.util.Text;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BasicParser {
    protected TokenStream tokens;
    protected Set<String> ignoredTokenTypes = new HashSet<>();
    protected Set<String> temporaryEndTokens = new HashSet<>();

    public BasicParser(TokenStream tokens) {
        this.tokens = Objects.requireNonNull(tokens);
    }

    public Source source() {
        return tokens.getSource();
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

    public Token lastToken() {
        int i = tokens.index - 1;
        while (true) {
            if (i < 0) return tokens.source.soi;

            final var token = tokens.tokens.get(i);
            if (!ignoredTokenTypes.contains(token.type)) {
                return token;
            }
            i -= 1;
        }
    }

    public Token currentToken() {
        consumeIgnoredTokens();
        if (atEnd()) return tokens.getSource().eoi;
        return tokens.peek();
    }

    public Token peek() {
        return currentToken();
    }

    //No consecutive _ws tokens. WhitespaceTokenType consumes all whitespace.
    public Token peek(int n) {
        int i = tokens.index + n;
        while (i < tokens.tokens.size()) { //At most off by 1 thus O(1)
            Token t = tokens.tokens.get(i);
            if (!ignoredTokenTypes.contains(t.type)) return t;
            i++;
        }
        return tokens.getSource().eoi;
    }

    public boolean peek(String... tokenTypes) {
        consumeIgnoredTokens();
        return tokens.peek(tokenTypes);
    }

    public boolean peek(String tokenType) {
        consumeIgnoredTokens();
        if (atEnd()) return false;
        return tokens.peek(tokenType);
    }

    public Token take() {
        consumeIgnoredTokens();
        if (atEnd()) return null;
        return tokens.take();
    }

    public boolean take(String tokenType) {
        if (!peek(tokenType)) return false;
        tokens.take();
        return true;
    }

    public Token consume(String... tokenTypes) {
        if (peek(tokenTypes)) {
            return tokens.take();
        } else {
            return null;
        }
    }

    public Token require(String tokenType, String message) {
        if (!peek(tokenType)) {
            var diag = new Diagnostic()
                    .note(message)
                    .note("instead found %s".formatted(Text.repr(currentToken().type)))
                    .highlight(currentToken().span);
            throw new ParseException(diag);
        }
        return take();
    }

    public String lex(Location start, Location end) {
        var subseq = tokens.content().subSequence(start.index, end.index);
        return subseq.toString();
    }

    public String sourceId() {
        return tokens.getSource().id;
    }

    // "pushes" the types onto the ignored type stack until this closable closes
    public IgnoredTypeGuard ignoreTypes(String... types) {
        return new IgnoredTypeGuard(types);
    }

    public Spanned restSpan() {
        return currentToken().span().through(source().eoi);
    }

    public Token expect(String tokenType, String errorMessage) {
        Token token = consume(tokenType);
        if (token == null) {
            final var diag = new Diagnostic()
                    .highlight(currentToken())
                    .note(errorMessage);
            throw new ParseException(diag);
        }
        return token;
    }

    public class IgnoredTypeGuard implements SafeAutoClosable {
        public final String[] ignoredTypes;
        private final boolean[] preExistingTypes;

        public IgnoredTypeGuard(String[] ignoredTypes) {
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
