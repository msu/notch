package bigsky.notch;

import bigsky.notch.expressions.*;
import bigsky.notch.statements.*;
import bigsky.utils.chisel.*;

import java.util.*;

import static bigsky.notch.token.NotchTokenTypeKeyword.NOTCH_KEYWORD;
import static bigsky.notch.token.TokenTypeTerseString.TERSE_STRING;
import static bigsky.utils.Exceptions.rethrow;
import static bigsky.utils.Text.repr;
import static bigsky.utils.chisel.type.TokenTypeBoolean.BOOL;
import static bigsky.utils.chisel.type.TokenTypeIdentifier.IDENT;
import static bigsky.utils.chisel.type.TokenTypeInteger.INT;
import static bigsky.utils.chisel.type.TokenTypePunct.*;
import static bigsky.utils.chisel.type.TokenTypeString.STR;
import static bigsky.utils.chisel.type.TokenTypeWhitespace.WHITESPACE;

public class NotchParser extends BasicParser {
    public NotchParser(TokenStream tokens) {
        super(tokens);
        ignoredTokenTypes.add(WHITESPACE);
    }

    public boolean peekIdent(String word) {
        if (!peek(IDENT)) return false;
        var token = tokens.peek();
        return token.str().equals(word);
    }

    public boolean takeIdent(String word) {
        if (!peekIdent(word)) return false;
        tokens.take();
        return true;
    }

    public Token requireIdent(String errMessage) {
        if (!peek(IDENT)) {
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
        if (!peek(NOTCH_KEYWORD)) return false;
        var token = tokens.peek();
        return token.str().equals(word);
    }

    public boolean takeKeyword(String word) {
        if (!peekKeyword(word)) return false;
        tokens.take();
        return true;
    }

    public Token requireKeyword(String errMessage) {
        if (!peek(NOTCH_KEYWORD)) {
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
            throw new ParseException(location(), message + ", found " + peek());
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

        // if the next keyword is an 'if' on the same line, it applies to the expressions
        if (takeKeywordOnSameLine("if", expr)) {
            var condition = parseFallbackExpr();
            if (condition == null) {
                throw new ParseException(location(), "expected condition after 'if' operator");
            }

            NotchExpression fallback = null;
            if (takeKeywordOnSameLine("else", expr)) {
                fallback = parseConditionalExpr();
                if (fallback == null) {
                    throw new ParseException(location(), "expected value after 'else' in 'if' expression");
                }
            }

            expr = new NotchConditional(expr, condition, fallback);
        }

        return expr;
    }

    private boolean takeKeywordOnSameLine(String keyword, NotchExpression expr) {
        Token nextToken = tokens.peek();
        boolean isMatch = tokens.match(nextToken, NOTCH_KEYWORD) &&
                nextToken.str().equals(keyword) &&
                nextToken.start.line == expr.end.line;
        if (isMatch) {
            tokens.take();
            return true;
        } else {
            return false;
        }
    }

    private NotchExpression parseFallbackExpr() {
        var expr = parseLogicalExpression();
        if (expr == null) return null;

        while (take(QUESTION_COLON)) {
            var fallback = parseLogicalExpression();
            if (fallback == null) {
                throw new ParseException(location(), "expected expression after '?:' operator");
            }

            expr = new NotchFallback(expr, fallback);
        }

        return expr;
    }

    private NotchExpression parseLogicalExpression() {
        NotchExpression expr = parseEqualityExpr();
        if (expr == null) return null;

        while ((peek(AMPERSAND2, BAR2) || peekIdent("and") || peekIdent("or"))) {
            Token op = take();
            var rhs = parseEqualityExpr();
            if (rhs == null) {
                throw new ParseException(location(), "expected expression after '+' operator");
            }
            expr = new NotchLogicalExpression(op, expr, rhs);
        }
        return expr;
    }

    private NotchExpression parseEqualityExpr() {
        var expr = parseComparisonExpression();
        if (expr == null) return null;

        while (peek(EQ2, BANG_EQ)) {
            Token op = take();
            var rhs = parseComparisonExpression();
            if (rhs == null) {
                throw new ParseException(location(), "expected expression after '" + op.str() + "' operator");
            }

            expr = new NotchEquality(op, expr, rhs);
        }

        return expr;
    }

    private NotchExpression parseComparisonExpression() {
        NotchExpression expr = parseAdditiveExpression();
        if (expr == null) return null;

        while (peek(LEFT_ARROW, LEFT_ARROW_EQ, RIGHT_ARROW, RIGHT_ARROW_EQ)) {
            Token op = take();
            var rhs = parseAdditiveExpression();
            if (rhs == null) {
                throw new ParseException(location(), "expected expression after '+' operator");
            }
            expr = new NotchComparisonExpression(op, expr, rhs);
        }
        return expr;
    }

    private NotchExpression parseAdditiveExpression() {
        NotchExpression expr = parseMultiplicativeExpression();
        while (peek(PLUS, DASH) && expr != null) {
            var opToken = take();

            var rhs = parseMultiplicativeExpression();
            if (rhs == null) {
                throw new ParseException(location(), "expected expression after '+' operator");
            }

            if (opToken.type.equals(PLUS)) {
                expr = new NotchAdditiveExpression(expr, rhs);
            } else {
                expr = new NotchSubtractionExpression(expr, rhs);
            }
        }
        return expr;
    }

    private NotchExpression parseMultiplicativeExpression() {
        NotchExpression expr = parseUnaryExpression();
        while (peek(STAR, SLASH, PERCENT) && expr != null) {
            var opToken = take();

            var rhs = parseUnaryExpression();
            if (rhs == null) {
                throw new ParseException(location(), "expected expression after '+' operator");
            }

            if (opToken.type.equals(STAR)) {
                expr = new NotchMultiplicationExpression(expr, rhs);
            } else if (opToken.type.equals(SLASH)) {
                expr = new NotchDivisionExpression(expr, rhs);
            } else {
                expr = new NotchRemainderExpression(expr, rhs);
            }
        }
        return expr;
    }

    private NotchExpression parseUnaryExpression() {
        Location start = location();
        if (takeKeyword("not") || take(BANG)) {
            NotchExpression expr = parseUnaryExpression();
            NotchNotExpression notExpr = new NotchNotExpression(start, location());
            notExpr.setExpression(expr);
            return notExpr;
        } else {
            return parseIndirectExpression();
        }
    }

    private NotchExpression parseIndirectExpression() {
        NotchExpression notchExpression = parsePrimaryExpression();
        if (notchExpression != null) {
            while (true) {
                if (peek(DOT)) {
                    notchExpression = parsePropertyAccessExpression(notchExpression);
                } else if (peek(LPAREN)) {
                    notchExpression = parseMethodInvocation(notchExpression);
                } else if (peek(LBRACKET)) {
                    notchExpression = parseIndexOperation(notchExpression);
                } else if (takeKeyword("is")) {
                    notchExpression = parseIsOperation(notchExpression);
                } else {
                    break;
                }
            }
        }
        return notchExpression;
    }

    private NotchExpression parseIsOperation(NotchExpression lhs) {
        var isInverted = takeKeyword("not");

        if (takeIdent("empty")) {
            return new IsEmptyExpression(lhs, isInverted, tokens.prev());
        } else {
            throw new ParseException(location(), "expected property after " + (isInverted ? "'is not'" : "'is'"));
        }
    }

    private NotchExpression parseIndexOperation(NotchExpression root) {
        if (take(LBRACKET)) {
            Location start = root.start;
            NotchExpression value = requireExpression("An expression is required");
            require(RBRACKET, "Index expressions must be closed with a ']'");
            NotchIndexExpression indexExpression = new NotchIndexExpression(start, location());
            indexExpression.setRoot(root);
            indexExpression.setValue(value);
            return indexExpression;
        }
        return null;
    }

    private NotchExpression parseMethodInvocation(NotchExpression root) {
        if (take(LPAREN)) {
            Location start = root.start;
            var args = new ArrayList<NotchExpression>();
            while (!atEnd() && !take(RPAREN)) {
                NotchExpression arg = parseExpression();
                args.add(arg);
                if (!peek(RPAREN)) {
                    if (!take(COMMA)) {
                        throw new ParseException(location(), "Expected ','");
                    }
                }
            }
            NotchMethodInvocation methodInvocation = new NotchMethodInvocation(start, location());
            if (root instanceof NotchPropertyAccess pa) {
                pa.setFavorMethods(true);
            }
            methodInvocation.setRoot(root);
            methodInvocation.setArgs(args);
            return methodInvocation;
        }
        return null;
    }

    private NotchExpression parsePropertyAccessExpression(NotchExpression root) {
        if (take(DOT)) {
            Location start = root.start;
            Token propName = requireIdent("Expected a property name");
            NotchPropertyAccess propAccess = new NotchPropertyAccess(start, location());
            propAccess.setProperty(propName);
            propAccess.setRoot(root);
            return propAccess;
        }
        return null;
    }

    private NotchExpression parsePrimaryExpression() {
        Token paren = consume(LPAREN);
        if (paren != null) {
            NotchExpression expr = parseExpression();
            require(RPAREN, "Expected a closing parenthesis");
            NotchParenthesizedExpression parenExpr = new NotchParenthesizedExpression(paren.start, location());
            parenExpr.setExpression(expr);
            return parenExpr;
        }

        Token bool = consume(BOOL);
        if (bool != null) {
            return new NotchBoolean(bool);
        }

        Token word = consume(IDENT);
        if (word != null) {
            return new NotchIdentifier(word);
        }

        Token intToken = consume(INT);
        if (intToken != null) {
            return new NotchInteger(intToken);
        }

        Token stringToken = consume(STR, TERSE_STRING);
        if (stringToken != null) {
            return new NotchString(stringToken);
        }

        if (peek(BACKSLASH)) {
            return parseClosureExpression();
        }

        if (peek(LBRACKET)) {
            return parseListLiteral();
        }

        if (peek(LBRACE)) {
            return parseMapLiteral();
        }

        return null;
    }

    private NotchExpression parseListLiteral() {
        Location start = location();
        if (take(LBRACKET)) {
            List<NotchExpression> listValues = new LinkedList<>();
            while (!atEnd() && !peek(RBRACKET)) {
                NotchExpression notchExpression = parseExpression();
                listValues.add(notchExpression);
                if (!peek(RBRACKET)) {
                    require(COMMA, "Expected a comma to separate elements in the list");
                } else {
                    take(COMMA); // allow a trailing comma
                }
            }
            require(RBRACKET, "Expected a ']' to close the list");
            NotchListLiteral notchListLiteral = new NotchListLiteral(start, location());
            notchListLiteral.setValues(listValues);
            return notchListLiteral;
        }
        return null;
    }

    private NotchExpression parseMapLiteral() {
        Location start = location();
        if (take(LBRACE)) {
            Map<String, NotchExpression> mapValues = new LinkedHashMap<>();
            while (!atEnd() && !peek(RBRACE)) {
                String key;
                if (peek(IDENT)) {
                    key = take().str();
                } else if (peek(STR)) {
                    key = String.valueOf(take().data);
                } else if (peek(TERSE_STRING)) {
                    key = String.valueOf(take().data);
                } else {
                    throw new ParseException(location(), "Expected a key");
                }
                require(EQ, "Expected a '=` to separate a key from a value in the map");
                NotchExpression notchExpression = parseExpression();
                mapValues.put(key, notchExpression);
                if (!peek(RBRACE)) {
                    require(COMMA, "Expected a comma to separate elements in the list");
                } else {
                    take(COMMA); // allow a trailing comma
                }
            }
            require(RBRACE, "Expected a '}' to close the map");
            NotchMapLiteral notchListLiteral = new NotchMapLiteral(start, location());
            notchListLiteral.setValues(mapValues);
            return notchListLiteral;
        }
        return null;
    }

    private NotchExpression parseClosureExpression() {
        Location start = location();
        if (take(BACKSLASH)) {
            List<Token> params = new ArrayList<>();
            while (!atEnd() && !peek(DASH_RIGHT_ARROW)) {
                Token param = require(IDENT, "Expected a parameter name");
                params.add(param);
                if (!peek(DASH_RIGHT_ARROW)) {
                    require(COMMA, "Expected a comma to separate parameters");
                }
            }
            require(DASH_RIGHT_ARROW, "Expected a '->' after the parameters");
            List<NotchStatement> statements = null;
            NotchExpression expression = null;
            if (take(LBRACE)) {
                statements = new LinkedList<>();
                while (!atEnd() && !peek(RBRACE)) {
                    NotchStatement stmt = parseStatement();
                    statements.add(stmt);
                }
                require(RBRACE, "Require a '}' to close the body of the closure");
            } else {
                expression = parseExpression();
            }
            NotchClosureExpression closureExpr = new NotchClosureExpression(start, location());
            closureExpr.setParameters(params);
            closureExpr.setExpression(expression);
            closureExpr.setStatements(statements);
            return closureExpr;
        }
        return null;
    }

    public NotchElement parse() {
        NotchExpression notchExpression;
        boolean tokensAfterExpr;
        Exception expressionException = new RuntimeException("Cannot parse this input");
        try {
            notchExpression = parseExpression();
            tokensAfterExpr = !tokens.atEnd();
            if (notchExpression != null && !tokensAfterExpr) {
                return notchExpression;
            }
        } catch (Exception e) {
            expressionException = e;
        }
        tokens.reset();
        NotchStatement notchStatement;
        try {
            notchStatement = parseAsStatement();
            if (notchStatement != null) {
                return notchStatement;
            } else {
                if (expressionException != null) {
                    throw expressionException;
                } else {
                    // TODO better errors
                    throw new RuntimeException("Cannot parse this input");
                }
            }
        } catch (Exception e) {
            if (expressionException != null) {
                throw rethrow(expressionException);
            } else {
                throw rethrow(e);
            }
        }
    }

    public NotchStatement parseAsStatement() {
        var stmts = new ArrayList<NotchStatement>();
        var start = tokens.location();
        while (!atEnd()) {
            stmts.add(parseStatement());
        }
        if (stmts.size() == 1) {
            return stmts.getFirst();
        } else {
            NotchStatements notchStatements = new NotchStatements(start, tokens.location());
            notchStatements.addChildren(stmts);
            return notchStatements;
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

        var assignmentStmt = parseAssignmentStatement();
        if (assignmentStmt != null) {
            return assignmentStmt;
        }

        throw new ParseException(tokens.location(), "expected statement");
    }

    private NotchStatement parseAssignmentStatement() {
        var start = tokens.location();
        if (peek(IDENT)) {
            Token varName = requireIdent("expected a variable name");
            require(EQ, "expected '='");
            NotchExpression valueExpression = requireExpression("expected expression for the loop iterable");
            NotchAssignment assignment = new NotchAssignment(start, tokens.location());
            assignment.setVariableName(varName);
            assignment.setExpression(valueExpression);
            return assignment;
        }
        return null;
    }

    private NotchForLoop parseForStatement() {
        var start = tokens.location();
        if (takeKeyword("for")) {

            Token loopIdentifier = requireIdent("expected a variable name for the loop item");
            requireIdent("in", "expected 'in'");

            NotchExpression loopExpression = requireExpression("expected expression for the loop iterable");

            Token indexIdentifier = null;
            if (takeIdent("index")) {
                indexIdentifier = requireIdent("expected a variable name for the ");
            }

            List<NotchStatement> loopBodyStatements = new ArrayList<>();

            while (!atEnd() && !peekKeyword("end")) {
                loopBodyStatements.add(parseStatement());
            }

            requireKeyword("end", "Unterminated for statement");

            NotchForLoop notchForLoop = new NotchForLoop(start, tokens.location());
            notchForLoop.setLoopVariable(loopIdentifier);
            notchForLoop.setIndexVariable(indexIdentifier);
            notchForLoop.setExpression(loopExpression);
            notchForLoop.setLoopBody(loopBodyStatements);
            return notchForLoop;
        }
        return null;
    }

    private NotchIf parseIfStatement() {
        var start = tokens.location();
        if (takeKeyword("if")) {

            NotchExpression conditional = parseExpression();
            if (conditional == null) {
                throw new ParseException(start, tokens.location(), "Expected a conditional expression");
            }

            var ifTrue = new ArrayList<NotchStatement>();
            while (!atEnd() && !(peekKeyword("end") || peekKeyword("else"))) {
                ifTrue.add(parseStatement());
            }

            var ifFalse = new ArrayList<NotchStatement>();
            if (takeKeyword("else")) {
                // TODO if the next token is 'if' and on the same line, it is a continuation of
                //      the current if and should be treated as syntactically bound to it
                while (!atEnd() && !peekKeyword("end")) {
                    ifFalse.add(parseStatement());
                }
            }

            requireKeyword("end", "Unterminated if statement");

            NotchIf notchIf = new NotchIf(start, tokens.location());
            notchIf.setExpression(conditional);
            notchIf.setIfTrue(ifTrue);
            notchIf.setIfFalse(ifFalse);
            return notchIf;
        }
        return null;
    }

    private NotchPrint parsePrintStatement() {
        var start = tokens.location();
        if (takeIdent("print")) {
            require(LPAREN, "arguments expected after 'print' keyword");
            NotchExpression expr = parseExpression();
            require(RPAREN, "missing argument terminator after 'print' arguments");
            NotchPrint notchPrint = new NotchPrint(start, tokens.location());
            notchPrint.setExpression(expr);
            return notchPrint;
        }
        return null;
    }
}
