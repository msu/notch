package bigsky.notch;

import bigsky.notch.expr.*;

import static bigsky.notch.NotchUtils.repr;

public class NotchParser {
    private Location location = new Location(0, 1, 1);
    private final String content;

    public NotchParser(String source, Location start) {
        this.location = start;
        this.content = source;
    }

    public NotchParser(String source) {
        content = source;
    }

    // if the current location, regardless of whitespace, as at EOF
    public boolean atEndImmediately() {
        return location.index() >= content.length();
    }

    public boolean atEnd() {
        consumeWhitespace();
        return atEndImmediately();
    }

    public boolean atEndN(int n) {
        consumeWhitespace();
        return location.index() + n >= content.length();
    }

    public void consumeWhitespace() {
        while (Character.isWhitespace(peek())) {
            advance();
        }
    }

    public char peek() {
        if (atEndImmediately()) return 0;
        return content.charAt(location.index());
    }

    public void advance() {
        if (atEndImmediately()) return;
        char c = content.charAt(location.index());
        if (c == '\n') {
            location = new Location(location.index() + 1, location.line() + 1, 1);
        } else {
            location = new Location(location.index() + 1, location.line(), location.column() + 1);
        }
    }

    public void advance(int n) {
        for (int i = 0; i < n && !atEndImmediately(); i++) {
            advance();
        }
    }

    public boolean peek(char c) {
        return peek() == c;
    }

    public char peekN(int n) {
        if (atEndN(n)) return 0;
        return content.charAt(location.index() + n);
    }

    public boolean peek(String s) {
        if (atEndN(s.length())) return false;

        char[] c = s.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] != peekN(i)) {
                return false;
            }
        }

        return true;
    }

    public boolean take(char c) {
        if (peek(c)) {
            advance();
            return true;
        }
        return false;
    }

    public boolean take(String s) {
        if (peek(s)) {
            advance(s.length());
            return true;
        }
        return false;
    }

    public NotchToken parseWord() {
        if (atEnd()) return null;

        var start = location;
        var word = new StringBuilder();

        char c = peek();
        if (!NotchUtils.isIdentifierStartChar(c)) {
            return null;
        }
        word.append(c);
        advance();

        while (!atEndImmediately()) {
            c = peek();
            if (NotchUtils.isIdentifierChar(c)) {
                word.append(c);
                advance();
            } else {
                break;
            }
        }

        return new NotchToken(start, location, word.toString(), "word");
    }

    public NotchToken takeWordT(String word) {
        var token = parseWord();
        if (token == null) return null;
        if (!token.lex.equals(word)) {
            location = token.start;
            return null;
        } else {
            return token;
        }
    }

    public boolean takeWord(String word) {
        return takeWordT(word) != null;
    }

    public void expectWord(String word, String contextMessage) {
        if (!takeWord(word)) {
            throw new ParseException(location, location, "expected " + repr(word) + ", " + contextMessage);
        }
    }

    public NotchExpression parseExpression() {
        return parseConditionalExpr();
    }

    private NotchExpression parseConditionalExpr() {
        var expr = parseFallbackExpr();
        if (expr == null) return null;

        if (takeWord("if")) {
            var condition = parseFallbackExpr();
            if (condition == null) {
                throw new ParseException(location, "expected condition after 'if' operator");
            }

            NotchExpression fallback = null;
            if (takeWord("else")) {
                fallback = parseConditionalExpr();
                if (fallback == null) {
                    throw new ParseException(location, "expected value after 'else' in 'if' expression");
                }
            }

            expr = new ConditionalExpression(expr, condition, fallback);
        }

        return expr;
    }

    private NotchExpression parseFallbackExpr() {
        var expr = parseEqualityExpr();
        if (expr == null) return null;

        while (take("?:")) {
            var fallback = parseEqualityExpr();
            if (fallback == null) {
                throw new ParseException(location, "expected expression after '?:' operator");
            }

            expr = new FallbackExpression(expr, fallback);
        }

        return expr;
    }

    private NotchExpression parseEqualityExpr() {
        var expr = parsePrimaryExpr();
        if (expr == null) return null;

        while (take("==")) {
            var rhs = parsePrimaryExpr();
            if (rhs == null) {
                throw new ParseException(location, "expected expression after '==' operator");
            }

            expr = new EqualityNotchExpression(expr, rhs);
        }

        return expr;
    }

    private NotchExpression parsePrimaryExpr() {
        NotchToken word = parseWord();
        if (word != null) {
            if (word.lex.equals("true") || word.lex.equals("false")) {
                return new BooleanNotchExpression(word);
            }
            return new IdentNotchExpression(word);
        }

        NotchToken intToken = parseInteger();
        if (intToken != null) {
            return new IntegerNotchExpression(intToken);
        }

        NotchToken stringToken = parseString();
        if (stringToken != null) {
            return new StringNotchExpression(stringToken);
        }

        return null;
    }

    public NotchToken parseString() {
        if (atEnd()) return null;

        var start = location;
        char c = peek();

        StringBuilder lex = new StringBuilder();
        if (c == ':') {
            advance();

            while (!atEndImmediately()) {
                c = peek();
                if (NotchUtils.isTerseCharacter(c)) {
                    advance();
                    lex.append(c);
                } else {
                    break;
                }
            }

            if (lex.isEmpty()) {
                throw new ParseException(location, "expected terse string character ':'");
            }

            return new NotchToken(start, location, lex.toString(), "string");
        } else if (c == '"' || c == '\'') {
            char quote = c;
            advance();

            while (!atEndImmediately()) {
                c = peek();
                if (c == quote || c == '\n') {
                    break;
                } else if (c != '\\') {
                    lex.append(c);
                    advance();
                } else {
                    advance();
                    if (atEndImmediately()) {
                        throw new ParseException(start, location, "invalid escape, expected something after '\\'");
                    }

                    c = peek();
                    advance();
                    if (c == '\\') {
                        lex.append('\\');
                    } else if (c == 'n') {
                        lex.append('\n');
                    } else if (c == 'r') {
                        lex.append('\r');
                    } else if (c == '"' && quote == '"') {
                        lex.append('"');
                    } else if (c == '\'' && quote == '\'') {
                        lex.append('\'');
                    } else {
                        throw new ParseException(start, location, "invalid escape " + repr(c));
                    }
                }
            }

            if (peek() != quote) {
                throw new ParseException(start, location, "unterminated string, expected " + quote);
            } else {
                advance();
            }

            return new NotchToken(start, location, lex.toString(), "string");
        } else {
            return null;
        }
    }

    public NotchToken parseInteger() {
        if (atEnd()) return null;
        var start = location;

        var lex = new StringBuilder();
        char c = peek();
        if (!Character.isDigit(c)) {
            return null;
        } else {
            lex.append(c);
            advance();
        }

        while (!atEndImmediately()) {
            c = peek();
            if (Character.isDigit(c)) {
                lex.append(c);
                advance();
            } else {
                break;
            }
        }

        return new NotchToken(start, location, lex.toString(), "int");
    }

    public Location getLocation() {
        return location;
    }
}
