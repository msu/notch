package edu.montana.notch;

import edu.montana.notch.chisel.*;
import edu.montana.notch.expressions.*;
import edu.montana.notch.statements.*;
import edu.montana.notch.templates.ast.QualifiedIdent;
import edu.montana.notch.util.Text;

import java.util.*;

import static edu.montana.notch.util.Exceptions.rethrow;
import static edu.montana.notch.util.Text.repr;

public class NotchParser extends BasicParser {
    private int loopDepth = 0;

    public NotchParser(TokenStream tokens) {
        super(tokens);
        ignoredTokenTypes.add("_ws");
    }

    public NotchParser(Source source) {
        this(Notch.TOKENIZER.tokenize(source));
    }

    public boolean peekIdent(String... idents) {
        if (!peek("ident")) return false;
        var token = tokens.peek();
        for (var ident : idents) {
            if (token.str().equals(ident)) return true;
        }
        return false;
    }

    public boolean takeIdent(String word) {
        if (!peekIdent(word)) return false;
        tokens.take();
        return true;
    }

    public Token requireIdent(String errMessage) {
        if (!peek("ident")) {
            var diag = new Diagnostic();
            diag.note(errMessage);
            diag.note("expected an identifier here");
            diag.highlight(tokens.peek());
            throw new ParseException(diag);
        }
        return tokens.take();
    }

    public void requireIdent(String word, String contextMessage) {
        if (!takeIdent(word)) {
            var diag = new Diagnostic();
            diag.note(contextMessage);
            diag.note("expected the identifier %s here".formatted(Text.repr(word)));
            diag.highlight(tokens.peek());
            throw new ParseException(diag);
        }
    }

    public boolean peekKeyword(String... words) {
        if (!peek("keyword")) return false;
        var token = tokens.peek();
        for (var word : words) {
            if (token.str().equals(word)) return true;
        }
        return false;
    }

    public boolean takeKeyword(String word) {
        if (!peekKeyword(word)) return false;
        tokens.take();
        return true;
    }

    public Token requireKeyword(String errMessage) {
        if (!peek("keyword")) {
            final var diag = new Diagnostic()
                    .note(errMessage)
                    .note("expected a keyword here")
                    .highlight(tokens.peek());
            throw new ParseException(diag);
        }
        return tokens.take();
    }

    public void requireKeyword(String word, String contextMessage) {
        if (!takeKeyword(word)) {
            final var diag = new Diagnostic()
                    .note(contextMessage)
                    .note(repr(word))
                    .highlight(tokens.peek());
            throw new ParseException(sourceId(), diag);
        }
    }

    public void requireEnd(String message) {
        if (!atEnd()) {
            final var diag = new Diagnostic()
                    .note(message)
                    .highlight(tokens.peek());
            throw new ParseException(diag);
        }
    }

    public NotchExpression parseExpression() {
        return parseConditionalExpr();
    }

    public NotchExpression requireExpression(String errorMessage) {
        final var start = this.tokens.index;
        NotchExpression expr;
        try {
            expr = parseExpression();
        } catch (ParseException e) {
            this.tokens.index = start;
            final var diag = new Diagnostic();
            diag.note(errorMessage);
            diag.highlight(tokens.peek());
            throw new ParseException(e, diag);
        }

        if (expr == null) {
            final var diag = new Diagnostic()
                    .note(errorMessage)
                    .highlight(tokens.peek());
            throw new ParseException(diag);
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
                final var diag = new Diagnostic();
                diag.note("expected condition after 'if' operator");
                diag.highlight(tokens.peek());
                throw new ParseException(diag);
            }

            NotchExpression fallback = null;
            if (takeKeywordOnSameLine("else", expr)) {
                fallback = parseConditionalExpr();
                if (fallback == null) {
                    final var diag = new Diagnostic();
                    diag.note("expected value after 'else' in 'if' expression");
                    diag.highlight(tokens.peek());
                    throw new ParseException(diag);
                }
            }

            expr = new NotchConditional(expr, condition, fallback);
        }

        return expr;
    }

    private boolean takeKeywordOnSameLine(String keyword, NotchExpression expr) {
        Token nextToken = tokens.peek();
        boolean isMatch = tokens.match(nextToken, "keyword") &&
                nextToken.str().equals(keyword) &&
                nextToken.start().line == expr.end().line;
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

        while (take("?:")) {
            var fallback = parseLogicalExpression();
            if (fallback == null) {
                final var diag = new Diagnostic()
                        .note("expected expression after '?:' operator")
                        .highlight(currentToken());
                throw new ParseException(diag);
            }
            expr = new NotchFallback(expr, fallback);
        }

        return expr;
    }

    private NotchExpression parseLogicalExpression() {
        NotchExpression expr = parseEqualityExpr();
        if (expr == null) return null;

        while (peek("&&", "||") || peekIdent("and", "or")) {
            Token op = take();
            var rhs = parseEqualityExpr();
            if (rhs == null) {
                final var diag = new Diagnostic()
                        .note("expected an expression after %s".formatted(repr(op.type)))
                        .highlight(op);
                throw new ParseException(diag);
            }
            expr = new NotchLogicalExpression(op, expr, rhs);
        }
        return expr;
    }

    private NotchExpression parseEqualityExpr() {
        var expr = parseComparisonExpression();
        if (expr == null) return null;

        while (peek("==", "!=")) {
            Token op = take();
            var rhs = parseComparisonExpression();
            if (rhs == null) {
                final var diag = new Diagnostic()
                        .note("expected expression after %s operator".formatted(repr(op.str())))
                        .highlight(currentToken());
                throw new ParseException(diag);
            }

            expr = new NotchEquality(op, expr, rhs);
        }

        return expr;
    }

    private NotchExpression parseComparisonExpression() {
        NotchExpression expr = parseAdditiveExpression();
        if (expr == null) return null;

        while (peek("<", "<=", ">", ">=")) {
            Token op = take();
            var rhs = parseAdditiveExpression();
            if (rhs == null) {
                final var diag = new Diagnostic()
                        .note("expected expression after '+' operator")
                        .highlight(currentToken());
                throw new ParseException(diag);
            }
            expr = new NotchComparisonExpression(op, expr, rhs);
        }
        return expr;
    }

    private NotchExpression parseAdditiveExpression() {
        NotchExpression expr = parseMultiplicativeExpression();
        while (peek("+", "-") && expr != null) {
            var opToken = take();

            var rhs = parseMultiplicativeExpression();
            if (rhs == null) {
                final var diag = new Diagnostic()
                        .note("expected expression after '+' operator")
                        .highlight(currentToken());
                throw new ParseException(diag);
            }

            if (opToken.type.equals("+")) {
                expr = new NotchAdditiveExpression(expr, rhs);
            } else {
                expr = new NotchSubtractionExpression(expr, rhs);
            }
        }
        return expr;
    }

    private NotchExpression parseMultiplicativeExpression() {
        NotchExpression expr = parseUnaryExpression();
        while (peek("*", "/", "%") && expr != null) {
            var opToken = take();

            var rhs = parseUnaryExpression();
            if (rhs == null) {
                final var diag = new Diagnostic()
                        .note("expected expression after '+' operator")
                        .highlight(currentToken());
                throw new ParseException(diag);
            }

            if (opToken.type.equals("*")) {
                expr = new NotchMultiplicationExpression(expr, rhs);
            } else if (opToken.type.equals("/")) {
                expr = new NotchDivisionExpression(expr, rhs);
            } else {
                expr = new NotchRemainderExpression(expr, rhs);
            }
        }
        return expr;
    }

    private NotchExpression parseUnaryExpression() {
        Span start = currentToken().span();
        if (takeKeyword("not") || take("!")) {
            NotchExpression expr = parseUnaryExpression();
            final var span = start.through(lastToken());
            NotchNotExpression notExpr = new NotchNotExpression(span, expr);
            return notExpr;
        } else if (take("-")) {
            NotchExpression expr = parseUnaryExpression();
            final var span = start.through(lastToken());
            var notExpr = new NotchNegateExpression(span, expr);
            return notExpr;
        } else {
            return parseIndirectExpression();
        }
    }

    private NotchExpression parseIndirectExpression() {
        NotchExpression notchExpression = parsePrimaryExpression();
        if (notchExpression != null) {
            while (true) {
                if (peek(".")) {
                    notchExpression = parsePropertyAccessExpression(notchExpression);
                } else if (peek("(")) {
                    notchExpression = parseMethodInvocation(notchExpression);
                } else if (peek("[")) {
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
            final var diag = new Diagnostic()
                    .note("expected property after " + (isInverted ? "'is not'" : "'is'"))
                    .highlight(currentToken());
            throw new ParseException(diag);
        }
    }

    private NotchExpression parseIndexOperation(NotchExpression root) {
        if (take("[")) {
            NotchExpression value = requireExpression("An expression is required");
            require("]", "Index expressions must be closed with a ']'");
            NotchIndexExpression indexExpression = new NotchIndexExpression(root, value, lastToken().end());
            return indexExpression;
        }
        return null;
    }

    private NotchExpression parseMethodInvocation(NotchExpression root) {
        if (take("(")) {
            var args = new ArrayList<NotchExpression>();
            while (!atEnd() && !peek(")")) {
                NotchExpression arg = parseExpression();
                args.add(arg);
                if (!peek(")")) {
                    if (!take(",")) {
                        final var diag = new Diagnostic()
                                .note("Expected ','")
                                .highlight(currentToken());
                        throw new ParseException(diag);
                    }
                }
            }
            if (!take(")")) {
                final var diag = new Diagnostic()
                        .note("Expected ')'")
                        .highlight(currentToken());
                throw new ParseException(diag);
            }
            NotchMethodInvocation methodInvocation = new NotchMethodInvocation(root, args, lastToken().end());
            if (root instanceof NotchPropertyAccess pa) {
                pa.setFavorMethods(true);
            }
            return methodInvocation;
        }
        return null;
    }

    private NotchExpression parsePropertyAccessExpression(NotchExpression root) {
        if (take(".")) {
            Token propName = requireIdent("Expected a property name");
            NotchPropertyAccess propAccess = new NotchPropertyAccess(root, propName);
            return propAccess;
        }
        return null;
    }

    public NotchString parseString() {
        Token stringToken = consume("string");
        if (stringToken != null) {
            return new NotchString(stringToken);
        }
        return null;
    }

    private NotchExpression parsePrimaryExpression() {
        Token paren = consume("(");
        if (paren != null) {
            NotchExpression expr = parseExpression();
            require(")", "Expected a closing parenthesis");
            final var span = expr.span.through(lastToken());
            NotchParenthesizedExpression parenExpr = new NotchParenthesizedExpression(span, expr);
            return parenExpr;
        }

        Token bool = consume("bool");
        if (bool != null) {
            return new NotchBoolean(bool);
        }

        Token word = consume("ident");
        if (word != null) {
            return new NotchIdentifier(word);
        }

        Token intToken = consume("int");
        if (intToken != null) {
            return new NotchInteger(intToken);
        }

        Token stringToken = consume("string");
        if (stringToken != null) {
            return new NotchString(stringToken);
        }

        if (peek("\\")) {
            return parseClosureExpression();
        }

        if (peek("[")) {
            return parseListLiteral();
        }

        if (peek("{")) {
            return parseMapLiteral();
        }

        if (peekKeyword("null")) {
            return new NullLiteral(take());
        }

        return null;
    }

    private NotchExpression parseListLiteral() {
        Token start = currentToken();
        if (take("[")) {
            List<NotchExpression> listValues = new LinkedList<>();
            while (!atEnd() && !peek("]")) {
                NotchExpression notchExpression = parseExpression();
                listValues.add(notchExpression);
                if (!peek("]")) {
                    require(",", "Expected a comma to separate elements in the list");
                } else {
                    take(","); // allow a trailing comma
                }
            }
            require("]", "Expected a ']' to close the list");
            NotchListLiteral notchListLiteral = new NotchListLiteral(start.span.through(lastToken()), listValues);
            return notchListLiteral;
        }
        return null;
    }

    private NotchExpression parseMapLiteral() {
        Span start = currentToken().span;
        if (take("{")) {
            Map<String, NotchExpression> mapValues = new LinkedHashMap<>();
            while (!atEnd() && !peek("}")) {
                String key;
                if (peek("ident")) {
                    key = take().str();
                } else if (peek("string")) {
                    key = String.valueOf(take().data);
                } else {
                    final var diag = new Diagnostic()
                            .note("expected a key")
                            .highlight(currentToken());
                    throw new ParseException(diag);
                }
                require("=", "Expected a '=` to separate a key from a value in the map");
                NotchExpression notchExpression = parseExpression();
                mapValues.put(key, notchExpression);
                if (!peek("}")) {
                    require(",", "Expected a comma to separate elements in the list");
                } else {
                    take(","); // allow a trailing comma
                }
            }
            require("}", "Expected a '}' to close the map");
            final var span = start.through(lastToken());
            NotchMapLiteral notchListLiteral = new NotchMapLiteral(span, mapValues);
            return notchListLiteral;
        }
        return null;
    }

    private NotchExpression parseClosureExpression() {
        if (take("\\")) {
            Token startToken = lastToken();
            List<Token> params = new ArrayList<>();
            while (!atEnd() && !peek("->")) {
                Token param = require("ident", "Expected a parameter name");
                params.add(param);
                if (!peek("->")) {
                    require(",", "Expected a comma to separate parameters");
                }
            }
            require("->", "Expected a '->' after the parameters");
            List<NotchStatement> statements = null;
            NotchExpression expression = null;
            if (take("{")) {
                statements = new LinkedList<>();
                while (!atEnd() && !peek("}")) {
                    NotchStatement stmt = parseStatement();
                    statements.add(stmt);
                }
                require("}", "Require a '}' to close the body of the closure");
            } else {
                expression = parseExpression();
            }
            final var span = startToken.span.through(lastToken());
            NotchClosureExpression closureExpr = new NotchClosureExpression(span, params, expression, statements);
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
        var start = currentToken();
        while (!atEnd()) {
            stmts.add(parseStatement());
        }
        if (stmts.size() == 1) {
            return stmts.get(0);
        } else {
            NotchStatements notchStatements = new NotchStatements(start.span.through(currentToken()), stmts);
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
        var breakStmt = parseBreakStatement();
        if (breakStmt != null) {
            return breakStmt;
        }
        var continueStmt = parseContinueStatement();
        if (continueStmt != null) {
            return continueStmt;
        }
        var repeatStmt = parseRepeatStatement();
        if (repeatStmt != null) {
            return repeatStmt;
        }
        var assignmentStmt = parseAssignmentStatement();
        if (assignmentStmt != null) {
            return assignmentStmt;
        }

        final var diag = new Diagnostic()
                .note("expected a statement")
                .highlight(currentToken());
        throw new ParseException(diag);
    }

    private NotchStatement parseRepeatStatement() {
        var start = tokens.location();
        if (!takeIdent("repeat")) return null;
        if (takeIdent("while")) {
            NotchExpression cond = requireExpression("expected condition after 'while'");
            List<NotchStatement> body = parseLoopBody();
            requireKeyword("end", "Unterminated repeat statement");
            final var span = new Span(source(), start, lastToken().end());
            return new NotchRepeatWhile(span, cond, body);
        }
        if (takeIdent("until")) {
            NotchExpression cond = requireExpression("expected condition after 'until'");
            List<NotchStatement> body = parseLoopBody();
            requireKeyword("end", "Unterminated repeat statement");
            final var span = new Span(source(), start, lastToken().end());
            return new NotchRepeatUntil(span, cond, body);
        }

        NotchExpression count = requireExpression("expected count expression after 'repeat'");
        if (!takeIdent("times")) {
            final var diag = new Diagnostic()
                    .note("expected 'times' after count expression in 'repeat'")
                    .highlight(currentToken());
            throw new ParseException(diag);
        }
        List<NotchStatement> body = parseLoopBody();
        requireKeyword("end", "Unterminated repeat statement");
        final var span = new Span(source(), start, lastToken().end());
        return new NotchRepeatTimes(span, count, body);
    }

    private List<NotchStatement> parseLoopBody() {
        List<NotchStatement> body = new ArrayList<>();
        try {
            loopDepth++;
            while (!atEnd() && !peekKeyword("end")) {
                body.add(parseStatement());
            }
        } finally {
            loopDepth--;
        }
        return body;
    }

    private NotchBreak parseBreakStatement() {
        var start = tokens.location();
        if (takeIdent("break")) {
            final var span = new Span(source(), start, lastToken().end());
            requireInLoop(span, "break");
            return new NotchBreak(span);
        }
        return null;
    }

    private NotchContinue parseContinueStatement() {
        var start = tokens.location();
        if (takeIdent("continue")) {
            final var span = new Span(source(), start, lastToken().end());
            requireInLoop(span, "continue");
            return new NotchContinue(span);
        }
        return null;
    }

    private void requireInLoop(Span span, String keyword) {
        if (loopDepth == 0) {
            final var diag = new Diagnostic()
                    .note("'" + keyword + "' outside a loop")
                    .highlight(span);
            throw new ParseException(diag);
        }
    }

    private NotchStatement parseAssignmentStatement() {
        if (peek("ident")) {
            Token varName = requireIdent("expected a variable name");
            require("=", "expected '='");
            NotchExpression valueExpression = requireExpression("expected expression for the loop iterable");
            NotchAssignment assignment = new NotchAssignment(varName, valueExpression);
            return assignment;
        }
        return null;
    }

    private NotchForLoop parseForStatement() {
        var start = tokens.location();
        if (takeKeyword("for")) {

            Token loopIdentifier = requireIdent("expected a variable name for the loop item");
            requireKeyword("in", "expected 'in'");

            NotchExpression loopExpression = requireExpression("expected expression for the loop iterable");

            Token indexIdentifier = null;
            if (takeIdent("index")) {
                indexIdentifier = requireIdent("expected a variable name for the ");
            }

            List<NotchStatement> loopBodyStatements = parseLoopBody();

            requireKeyword("end", "Unterminated for statement");

            final var span = new Span(source(), start, lastToken().end());
            NotchForLoop notchForLoop = new NotchForLoop(span, loopIdentifier, loopExpression, indexIdentifier, loopBodyStatements);
            return notchForLoop;
        }
        return null;
    }

    private NotchIf parseIfStatement() {
        var start = tokens.location();
        if (takeKeyword("if")) {

            NotchExpression conditional = parseExpression();
            if (conditional == null) {
                final var diag = new Diagnostic()
                        .note("expected a conditional expression after 'if'")
                        .highlight(lastToken());
                throw new ParseException(diag);
            }

            var ifTrue = new ArrayList<NotchStatement>();
            while (!atEnd() && !peekKeyword("end", "else")) {
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

            final var span = new Span(source(), start, lastToken().end());
            NotchIf notchIf = new NotchIf(span, conditional, ifTrue, ifFalse);
            return notchIf;
        }
        return null;
    }

    private NotchPrint parsePrintStatement() {
        var start = tokens.location();
        if (takeIdent("print")) {
            require("(", "arguments expected after 'print' keyword");
            NotchExpression expr = parseExpression();
            require(")", "missing argument terminator after 'print' arguments");
            final var span = new Span(source(), start, lastToken().end());
            NotchPrint notchPrint = new NotchPrint(span, expr);
            return notchPrint;
        }
        return null;
    }

    public QualifiedIdent parseQualifiedIdent() {
        return QualifiedIdent.parse(this);
    }

    public QualifiedIdent requireQualifiedIdent(String errorMessage) {
        final var ident = parseQualifiedIdent();
        if (ident == null) {
            final var diag = new Diagnostic()
                    .highlight(currentToken())
                    .note(errorMessage);
            throw new ParseException(diag);
        }
        return ident;
    }
}
