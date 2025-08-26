package bigsky.notch;

import bigsky.notch.expr.*;
import bigsky.notch.stmt.*;

import java.util.ArrayList;
import java.util.List;

import static bigsky.utils.Text.repr;

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

    public void require(char c) {
        if(!take(c)){
            throw new ParseException(location, "Expected " + c);
        }
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

    public NotchToken takeWord() {
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
        var token = takeWord();
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

    public boolean peekWord(String word) {
        var token = takeWordT(word);
        if (token == null) return false;
        location = token.start;
        return true;
    }

    public void requireWord(String word, String contextMessage) {
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
        NotchToken word = takeWord();
        if (word != null) {
            if (word.lex.equals("true") || word.lex.equals("false")) {
                return new BooleanNotchExpression(word);
            }
            return new IdentNotchExpression(word);
        }

        NotchToken intToken = takeInteger();
        if (intToken != null) {
            return new IntegerNotchExpression(intToken);
        }

        NotchToken stringToken = takeString();
        if (stringToken != null) {
            return new StringNotchExpression(stringToken);
        }

        return null;
    }

    public NotchToken takeString() {
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

    public NotchToken takeInteger() {
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

    public NotchStatement parse() {
        var stmts = new ArrayList<NotchStatement>();
        var start = location;
        while (!atEnd()) {
            stmts.add(parseStatement());
        }
        if(stmts.size() == 1) {
            return stmts.getFirst();
        } else {
            StatementList statementList = new StatementList(start, location);
            stmts.forEach(statementList::addStatement);
            return statementList;
        }
    }

    private NotchStatement parseStatement() {
        var print = parsePrintStatement();
        if(print != null) {
            return print;
        }
        var ifStmt = parseIfStatement();
        if(ifStmt != null) {
            return ifStmt;
        }
        var forStmt = parseForStatement();
        if(forStmt != null) {
            return forStmt;
        }
        throw new ParseException(location, "expected statement");
    }

    private ForStatement parseForStatement() {
        Location start = location;
        if (takeWord("for")) {

            NotchToken loopIdentifier = takeWord();
            if (loopIdentifier == null) {
                throw new ParseException(location, "expected loop identifier");
            }

            requireWord("in", "expected 'in'");

            NotchExpression loopExpression = parseExpression();
            if (loopExpression == null) {
                throw new ParseException(location, "expected loop expression");
            }

            NotchToken indexIdentifier = null;
            if (takeWord("index")) {
                indexIdentifier = takeWord();
                if (indexIdentifier == null) {
                    throw new ParseException(location, "expected index identifier");
                }
            }

            List<NotchStatement> loopBodyStatements = new ArrayList<>();

            while(!atEnd() && !peekWord("end")) {
                loopBodyStatements.add(parseStatement());
            }

            requireWord("end", "Unterminated for statement");

            ForStatement forStatement = new ForStatement(start, location);
            forStatement.setLoopVariable(loopIdentifier);
            forStatement.setIndexVariable(indexIdentifier);
            forStatement.setExpression(loopExpression);
            loopBodyStatements.forEach(forStatement::addLoopBodyStatement);
            return forStatement;
        }
        return null;
    }

    private IfStatement parseIfStatement() {
        Location start = location;
        if (takeWord("if")) {

            NotchExpression conditional = parseExpression();
            if(conditional == null) {
                throw new ParseException(start, location, "Expected a conditional expression");
            }

            var ifTrue = new ArrayList<NotchStatement>();
            while(!atEnd() && !(peekWord("end") || peekWord("else"))) {
                ifTrue.add(parseStatement());
            }

            var ifFalse = new ArrayList<NotchStatement>();
            if(takeWord("else")) {
                // TODO if the next token is 'if' and on the same line, it is a continuation of
                //      the current if and should be treated as syntactically bound to it
                while(!atEnd() && !peekWord("end")) {
                    ifFalse.add(parseStatement());
                }
            }

            requireWord("end", "Unterminated if statement");

            IfStatement ifStatement = new IfStatement(start, location);
            ifStatement.setExpression(conditional);
            ifTrue.forEach(ifStatement::addTrueStatement);
            ifFalse.forEach(ifStatement::addFalseStatement);
            return ifStatement;
        }
        return null;
    }

    private PrintStatement parsePrintStatement() {
        Location start = location;
        if (takeWord("print")) {
            require('(');
            NotchExpression expr = parseExpression();
            require(')');
            PrintStatement printStatement = new PrintStatement(start, location);
            printStatement.setExpression(expr);
            return printStatement;
        }
        return null;
    }
}
