package bigsky.notch;

import bigsky.notch.expr.*;
import bigsky.notch.stmt.*;
import bigsky.utils.chisel.*;

import java.util.ArrayList;
import java.util.List;

import static bigsky.utils.BigSkyUtils.repr;

public class NotchParser extends BasicParser {
    public NotchParser(TokenStream tokens) {
        super(tokens);
        ignoredTokenTypes.add("whitespace");
    }

    public boolean peekIdent(String word) {
        if (!peek("ident")) return false;
        var token = tokens.peek();
        return token.str().equals(word);
    }

    public boolean takeIdent(String word) {
        if (!peekIdent(word)) return false;
        tokens.take();
        return true;
    }

    public Token requireIdent(String errMessage) {
        if (!peek("ident")) {
            var loc = tokens.location();
            throw new ParseException(loc, errMessage + ": (expected identifier)");
        }
        return tokens.take();
    }

    public void requireIdent(String word, String contextMessage) {
        if (!takeIdent(word)) {
            var loc = tokens.location();
            throw new ParseException(loc, loc, contextMessage + " : (expected " + repr(word) + ")");
        }
    }

    public boolean peekKeyword(String word) {
        if (!peek("keyword")) return false;
        var token = tokens.peek();
        return token.str().equals(word);
    }

    public boolean takeKeyword(String word) {
        if (!peekKeyword(word)) return false;
        tokens.take();
        return true;
    }

    public Token requireKeyword(String errMessage) {
        if (!peek("keyword")) {
            var loc = tokens.location();
            throw new ParseException(loc, errMessage + ": (expected identifier)");
        }
        return tokens.take();
    }

    public void requireKeyword(String word, String contextMessage) {
        if (!takeKeyword(word)) {
            var loc = tokens.location();
            throw new ParseException(loc, loc, contextMessage + " : (expected " + repr(word) + ")");
        }
    }

    public void requireEnd(String message) {
        if (!atEnd()) {
            throw new ParseException(location(), message + ", found " + peek().type.label());
        }
    }

    public NotchExpression parseExpression() {
        return parseConditionalExpr();
    }

    public NotchExpression requireExpression(String errorMessage) {
        NotchExpression expr;
        try {
            expr = parseExpression();
        } catch (ParseException e) {
            throw new ParseException(location(), errorMessage, e);
        }

        if (expr == null) {
            throw new ParseException(location(), errorMessage + ", expected expression");
        }
        return expr;
    }

    private NotchExpression parseConditionalExpr() {
        var expr = parseFallbackExpr();
        if (expr == null) return null;

        if (takeKeyword("if")) {
            var condition = parseFallbackExpr();
            if (condition == null) {
                throw new ParseException(location(), "expected condition after 'if' operator");
            }

            NotchExpression fallback = null;
            if (takeKeyword("else")) {
                fallback = parseConditionalExpr();
                if (fallback == null) {
                    throw new ParseException(location(), "expected value after 'else' in 'if' expression");
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
                throw new ParseException(location(), "expected expression after '?:' operator");
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
                throw new ParseException(location(), "expected expression after '==' operator");
            }

            expr = new EqualityNotchExpression(expr, rhs);
        }

        return expr;
    }

    private NotchExpression parsePrimaryExpr() {
        Token bool = consume("bool");
        if (bool != null) {
            return new BooleanNotchExpression(bool);
        }

        Token word = consume("ident");
        if (word != null) {
            return new IdentNotchExpression(word);
        }

        Token intToken = consume("int");
        if (intToken != null) {
            return new IntegerNotchExpression(intToken);
        }

        Token stringToken = consume("string");
        if (stringToken != null) {
            return new StringNotchExpression(stringToken);
        }

        return null;
    }

    public NotchStatement parse() {
        var stmts = new ArrayList<NotchStatement>();
        var start = tokens.location();
        while (!atEnd()) {
            stmts.add(parseStatement());
        }
        if (stmts.size() == 1) {
            return stmts.getFirst();
        } else {
            StatementList statementList = new StatementList(start, tokens.location());
            stmts.forEach(statementList::addStatement);
            return statementList;
        }
    }

    private NotchStatement parseStatement() {
        var print = parsePrintStatement();
        if (print != null) {
            return print;
        }
        var ifStmt = parseIfStatement();
        if (ifStmt != null) {
            return ifStmt;
        }
        var forStmt = parseForStatement();
        if (forStmt != null) {
            return forStmt;
        }
        throw new ParseException(tokens.location(), "expected statement");
    }

    private ForStatement parseForStatement() {
        var start = tokens.location();
        if (takeKeyword("for")) {

            Token loopIdentifier = requireIdent("expected a variable name for the loop item");
            requireIdent("in", "expected 'in'");

            NotchExpression loopExpression = requireExpression("expected expression for the loop iterable");

            Token indexIdentifier = null;
            if (takeKeyword("index")) {
                indexIdentifier = requireIdent("expected a variable name for the ");
            }

            List<NotchStatement> loopBodyStatements = new ArrayList<>();

            while (!atEnd() && !peekIdent("end")) {
                loopBodyStatements.add(parseStatement());
            }

            requireIdent("end", "Unterminated for statement");

            ForStatement forStatement = new ForStatement(start, tokens.location());
            forStatement.setLoopVariable(loopIdentifier);
            forStatement.setIndexVariable(indexIdentifier);
            forStatement.setExpression(loopExpression);
            loopBodyStatements.forEach(forStatement::addLoopBodyStatement);
            return forStatement;
        }
        return null;
    }

    private IfStatement parseIfStatement() {
        var start = tokens.location();
        if (takeKeyword("if")) {

            NotchExpression conditional = parseExpression();
            if (conditional == null) {
                throw new ParseException(start, tokens.location(), "Expected a conditional expression");
            }

            var ifTrue = new ArrayList<NotchStatement>();
            while (!atEnd() && !(peekIdent("end") || peekIdent("else"))) {
                ifTrue.add(parseStatement());
            }

            var ifFalse = new ArrayList<NotchStatement>();
            if (takeKeyword("else")) {
                // TODO if the next token is 'if' and on the same line, it is a continuation of
                //      the current if and should be treated as syntactically bound to it
                while (!atEnd() && !peekIdent("end")) {
                    ifFalse.add(parseStatement());
                }
            }

            requireKeyword("end", "Unterminated if statement");

            IfStatement ifStatement = new IfStatement(start, tokens.location());
            ifStatement.setExpression(conditional);
            ifTrue.forEach(ifStatement::addTrueStatement);
            ifFalse.forEach(ifStatement::addFalseStatement);
            return ifStatement;
        }
        return null;
    }

    private PrintStatement parsePrintStatement() {
        var start = tokens.location();
        if (takeIdent("print")) {
            require("(", "arguments expected after 'print' keyword");
            NotchExpression expr = parseExpression();
            require(")", "missing argument terminator after 'print' arguments");
            PrintStatement printStatement = new PrintStatement(start, tokens.location());
            printStatement.setExpression(expr);
            return printStatement;
        }
        return null;
    }
}
