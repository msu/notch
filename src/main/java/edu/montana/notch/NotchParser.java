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
    private int functionDepth = 0;
    private int catchDepth = 0;

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
        return parseRecoverExpr();
    }

    private NotchExpression parseRecoverExpr() {
        NotchExpression tryExpr = parseConditionalExpr();
        if (tryExpr == null) return null;
        if (atEnd()) return tryExpr;
        if (tokens.peek().startLine() != tryExpr.endLine()) return tryExpr;

        if (peekKeyword("catch")) {
            var diag = new Diagnostic()
                    .note("'catch' is not allowed in a recover expression")
                    .note("use a try/catch block to catch exceptions with side effects")
                    .highlight(currentToken());
            throw new ParseException(diag);
        }

        if (!peekKeyword("recover")) return tryExpr;

        List<NotchRecoverExpression.TypedRecover> typedRecovers = new ArrayList<>();
        NotchExpression untypedRecover = null;
        int lastEndLine = tryExpr.endLine();
        while (!atEnd()) {
            boolean sameLineAsLast = currentToken().startLine() == lastEndLine;
            if (!sameLineAsLast && !peekKeyword("recover")) break;
            if (peekKeyword("recover")) {
                take();
                boolean isTyped = false;
                if (takeIdent("from")) {
                    isTyped = true;
                } else if (peek("ident") && !peekIdent("with")) {
                    Token first  = peek();
                    Token second = peek(1);
                    isTyped = !second.type.equals("eoi")
                            && second.startLine() == first.endLine()
                            && !second.type.equals("(");
                }
                if (isTyped) {
                    QualifiedIdent type = parseQualifiedIdent();
                    takeIdent("with");
                    NotchExpression expr = parseConditionalExpr();
                    if (expr == null) {
                        throw new ParseException(new Diagnostic().note("expected expression after recover type"));
                    }
                    typedRecovers.add(new NotchRecoverExpression.TypedRecover(type, expr));
                    lastEndLine = lastToken().endLine();
                } else {
                    takeIdent("with");
                    untypedRecover = requireExpression("expected an expression after 'recover'");
                    break;
                }
            } else {
                var diag = new Diagnostic()
                        .note("unexpected token after recover: expected 'recover' or end of line")
                        .highlight(currentToken());
                throw new ParseException(diag);
            }
        }
        return new NotchRecoverExpression(tryExpr.span.through(lastToken()), tryExpr, typedRecovers, untypedRecover);
    }

    private NotchStatement requireSameLineStatement() {
        int startLine = currentToken().startLine();
        NotchStatement stmt = parseStatement();
        if (lastToken().endLine() != startLine) {
            final var diag = new Diagnostic()
                    .note("inline catch statement fell onto the next line; use a multiline 'catch ... end' block")
                    .highlight(stmt.span());
            throw new ParseException(diag);
        }
        return stmt;
    }

    public NotchExpression requireExpression(String errorMessage) {
        NotchExpression expr = parseExpression();
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

        while (peek("==", "!=") || peekKeyword("is")) {
            //"is" precedent level "(a + b) is empty" correct
            if (takeKeyword("is")) {
                expr = parseIsOperation(expr);
                continue;
            }
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

        while (peek("<", "<=", ">", ">=")
                || peekIdent("starts", "ends", "contains")) {
            if (peek("<", "<=", ">", ">=")) {
                Token op = take();
                var rhs = parseAdditiveExpression();
                if (rhs == null) {
                    final var diag = new Diagnostic()
                            .note("expected expression after comparison operator")
                            .highlight(currentToken());
                    throw new ParseException(diag);
                }
                expr = new NotchComparisonExpression(op, expr, rhs);
            } else if (takeIdent("starts")) {
                requireIdent("with", "expected 'with' after 'starts'");
                var rhs = parseAdditiveExpression();
                if (rhs == null) {
                    throw new ParseException(new Diagnostic()
                            .note("expected expression after 'starts with'")
                            .highlight(currentToken()));
                }
                expr = new NotchStartsWithExpression(expr, rhs);
            } else if (takeIdent("ends")) {
                requireIdent("with", "expected 'with' after 'ends'");
                var rhs = parseAdditiveExpression();
                if (rhs == null) {
                    throw new ParseException(new Diagnostic()
                            .note("expected expression after 'ends with'")
                            .highlight(currentToken()));
                }
                expr = new NotchEndsWithExpression(expr, rhs);
            } else if (takeIdent("contains")) {
                var rhs = parseAdditiveExpression();
                if (rhs == null) {
                    throw new ParseException(new Diagnostic()
                            .note("expected expression after 'contains'")
                            .highlight(currentToken()));
                }
                expr = new NotchContainsExpression(expr, rhs);
            }
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
                } else {
                    break;
                }
            }
        }
        return notchExpression;
    }

    private NotchExpression parseIsOperation(NotchExpression lhs) {
        var isInverted = takeKeyword("not");
        var isToken = lastToken();
        if (peekIdent("empty")) {
            int beforeEmpty = tokens.index;
            tokens.take();
            if (!peek("(") && !peek(".")) {
                return new IsEmptyExpression(lhs, isInverted, tokens.prev());
            }
            tokens.index = beforeEmpty;
        }
        NotchExpression rhs = parseComparisonExpression();
        if (rhs == null) {
            throw new ParseException(new Diagnostic()
                    .note("expected expression after " + (isInverted ? "'is not'" : "'is'"))
                    .highlight(currentToken()));
        }
        String opStr = isInverted ? "!=" : "==";
        Token Op = new Token(isToken.span(), opStr, opStr);
        return new NotchEquality(Op, lhs, rhs);
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
            return parseBraceLiteral();
        }

        if (peekKeyword("new")) {
            return parseInstantiation();
        }

        if (peekKeyword("this")) {
            return new NotchIdentifier(take());
        }

        if (peekKeyword("null")) {
            return new NullLiteral(take());
        }

        return null;
    }

    private NotchExpression parseInstantiation() {
        var start = tokens.location();
        if (!takeKeyword("new")) return null;
        Token className = requireIdent("expected a class name after 'new'");
        require("(", "expected '(' after class name");
        List<NotchExpression> args = parseArgList();
        require(")", "expected ')' to close the argument list");
        final var span = new Span(source(), start, lastToken().end());
        return new NotchInstantiation(span, className, args);
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

    private NotchExpression parseBraceLiteral() {
        Span start = currentToken().span;
        if (!take("{")) return null;
        if (take("}")) {
            return new NotchMapLiteral(start.through(lastToken()), new ArrayList<>(), new ArrayList<>());
        }
        if (take(",")) {
            require("}", "expected '}' to close the empty set '{,}'");
            return new NotchSetLiteral(start.through(lastToken()), new ArrayList<>());
        }
        NotchExpression first = parseExpression();
        if (take("->")) {
            List<NotchExpression> keys = new ArrayList<>();
            List<NotchExpression> values = new ArrayList<>();
            keys.add(first);
            values.add(parseExpression());
            while (take(",")) {
                if (peek("}")) break;
                keys.add(parseExpression());
                require("->", "expected '->' between a map key and value");
                values.add(parseExpression());
            }
            require("}", "expected '}' to close the map");
            return new NotchMapLiteral(start.through(lastToken()), keys, values);
        } else {
            List<NotchExpression> values = new ArrayList<>();
            values.add(first);
            while (take(",")) {
                if (peek("}")) break;
                values.add(parseExpression());
            }
            require("}", "expected '}' to close the set");
            return new NotchSetLiteral(start.through(lastToken()), values);
        }
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
        Exception expressionException = null;
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
        var functionStmt = parseFunctionDeclaration();
        if (functionStmt != null) {
            return functionStmt;
        }
        var returnStmt = parseReturnStatement();
        if (returnStmt != null) {
            return returnStmt;
        }
        var throwStmt = parseThrowStatement();
        if (throwStmt != null) {
            return throwStmt;
        }
        var tryStmt = parseTryStatement();
        if (tryStmt != null) {
            return tryStmt;
        }
        var rethrowStmt = parseRethrowStatement();
        if (rethrowStmt != null) {
            return rethrowStmt;
        }
        var classStmt = parseClassDeclaration();
        if (classStmt != null) {
            return classStmt;
        }
        var assignmentStmt = parseAssignmentStatement();
        if (assignmentStmt != null) {
            return assignmentStmt;
        }
        var callStmt = parseCallStatement();
        if (callStmt != null) {
            return callStmt;
        }

        final var diag = new Diagnostic()
                .note("expected a statement")
                .highlight(currentToken());
        throw new ParseException(diag);
    }

    private NotchStatement parseCallStatement() {
        int mark = tokens.index;
        try {
            NotchExpression expr = parseExpression();
            if (expr instanceof NotchMethodInvocation || expr instanceof NotchRecoverExpression) {
                return new NotchExpressionStatement(expr);
            }
        } catch (ParseException e) {
            // TODO: replace with synchronization
            if (tokens.index > mark) {
                throw e;
            }
        }
        tokens.index = mark;
        return null;
    }

    private NotchStatement parseThrowStatement() {
        var start = tokens.location();
        if (!takeKeyword("throw")) return null;
        Token throwToken = lastToken();
        if (atEnd() || currentToken().startLine() != throwToken.endLine()) {
            final var diag = new Diagnostic()
                    .note("expected an expression after 'throw' on the same line")
                    .highlight(currentToken());
            throw new ParseException(diag);
        }
        NotchExpression operand = requireExpression("expected an expression to throw");
        final var span = new Span(source(), start, lastToken().end());
        return new NotchThrowStatement(span, operand);
    }

    private NotchStatement parseRethrowStatement() {
        var start = tokens.location();
        if (!takeKeyword("rethrow")) return null;
        final var span = new Span(source(), start, lastToken().end());
        if (catchDepth == 0) {
            final var diag = new Diagnostic()
                    .note("'rethrow' outside a catch")
                    .highlight(span);
            throw new ParseException(diag);
        }
        return new NotchRethrow(span);
    }

    private NotchStatement parseTryStatement() {
        var start = tokens.location();
        if (!takeKeyword("try")) return null;

        List<NotchStatement> body = new ArrayList<>();
        while (!atEnd() && !peekKeyword("catch", "end")) {
            body.add(parseStatement());
        }

        List<NotchCatch> clauses = new ArrayList<>();
        while (peekKeyword("catch")) {
            Token catchToken = take();
            QualifiedIdent type = null;
            Token exceptionName = null;
            int nextTokenLine = currentToken().startLine();
            int catchTokenLine = catchToken.endLine();
            boolean tokensFollow = !atEnd() && nextTokenLine == catchTokenLine;
            if (tokensFollow && peek("ident")) {
                type = requireQualifiedIdent("expected exception type after 'catch' e.g. 'catch IOException'");
                if (takeKeyword("as")) {
                    exceptionName = requireIdent("expected a binding name after 'as' e.g. 'catch IOException as e'");
                } else if (!atEnd() && currentToken().startLine() == catchTokenLine) {
                    var diag = new Diagnostic();
                    diag.note("catch body must start on a new line");
                    diag.note("to bind the exception use 'catch IOException as e'");
                    diag.highlight(currentToken());
                    throw new ParseException(diag);
                }
            }
            //implicit catch all
            List<NotchStatement> catchBody = new ArrayList<>();
            catchDepth++;
            try {
                while (!atEnd() && !peekKeyword("catch", "end")) {
                    catchBody.add(parseStatement());
                }
            } finally {
                catchDepth--;
            }
            String name = exceptionName == null ? null : exceptionName.str();
            clauses.add(new NotchCatch(type, name, catchBody));
        }

        requireKeyword("end", "unterminated 'try', expected 'end'");
        final var span = new Span(source(), start, lastToken().end());
        return new NotchTry(span, body, clauses);
    }

    private NotchStatement parseRepeatStatement() {
        var start = tokens.location();
        if (!takeKeyword("repeat")) return null;
        if (takeKeyword("while")) {
            NotchExpression cond = requireExpression("expected condition after 'while'");
            List<NotchStatement> body = parseLoopBody();
            requireKeyword("end", "Unterminated repeat statement");
            final var span = new Span(source(), start, lastToken().end());
            return new NotchRepeatWhile(span, cond, body);
        }
        if (takeKeyword("until")) {
            NotchExpression cond = requireExpression("expected condition after 'until'");
            List<NotchStatement> body = parseLoopBody();
            requireKeyword("end", "Unterminated repeat statement");
            final var span = new Span(source(), start, lastToken().end());
            return new NotchRepeatUntil(span, cond, body);
        }

        NotchExpression count = requireExpression("expected count expression after 'repeat'");
        if (!takeKeyword("times")) {
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
        if (takeKeyword("break")) {
            final var span = new Span(source(), start, lastToken().end());
            requireInLoop(span, "break");
            return new NotchBreak(span);
        }
        return null;
    }

    private NotchContinue parseContinueStatement() {
        var start = tokens.location();
        if (takeKeyword("continue")) {
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

    private void requireInFunction(Span span, String keyword) {
        if (functionDepth == 0) {
            final var diag = new Diagnostic()
                    .note("'" + keyword + "' outside a function")
                    .highlight(span);
            throw new ParseException(diag);
        }
    }

    private List<NotchField> parseParamList() {
        List<NotchField> params = new ArrayList<>();
        while (!atEnd() && !peek(")")) {
            Token name = requireIdent("expected a parameter name");
            QualifiedIdent type = null;
            if (take(":")) {
                type = requireQualifiedIdent("expected a parameter type after ':'");
            }
            params.add(new NotchField(name, type, null));
            if (!peek(")")) {
                require(",", "expected ',' to separate parameters");
            }
        }
        return params;
    }

    private List<NotchExpression> parseArgList() {
        List<NotchExpression> args = new ArrayList<>();
        while (!atEnd() && !peek(")")) {
            args.add(parseExpression());
            if (!peek(")")) {
                require(",", "expected ',' to separate arguments");
            }
        }
        return args;
    }

    private NotchStatement parseFunctionDeclaration() {
        var start = tokens.location();
        if (!takeKeyword("function")) return null;
        Token name = requireIdent("expected a function name");
        require("(", "expected '(' after function name");
        List<NotchField> params = parseParamList();
        require(")", "expected ')' to close the parameter list");
        QualifiedIdent returnType = null;
        if (take(":")) {
            returnType = requireQualifiedIdent("expected a return type after ':'");
        }
        int savedLoopDepth = loopDepth;
        loopDepth = 0;
        functionDepth++;
        List<NotchStatement> body = new ArrayList<>();
        try {
            while (!atEnd() && !peekKeyword("end")) {
                body.add(parseStatement());
            }
        } finally {
            functionDepth--;
            loopDepth = savedLoopDepth;
        }
        requireKeyword("end", "unterminated function, expected 'end'");
        final var span = new Span(source(), start, lastToken().end());
        return new NotchFunctionDeclaration(span, name, params, returnType, body);
    }

    private NotchStatement parseReturnStatement() {
        var start = tokens.location();
        if (!takeKeyword("return")) return null;
        Token returnToken = lastToken();
        final var keywordSpan = new Span(source(), start, returnToken.end());
        requireInFunction(keywordSpan, "return");

        NotchExpression value = null;
        if (returnValueFollows(returnToken)) {
            value = parseExpression();
        }
        final var span = new Span(source(), start, lastToken().end());
        return new NotchReturn(span, value);
    }

    private boolean returnValueFollows(Token returnToken) {
        return !atEnd()
                && !peekKeyword("end", "else")
                && currentToken().startLine() == returnToken.endLine();
    }

    private NotchStatement parseClassDeclaration() {
        var start = tokens.location();
        if (!takeKeyword("class")) return null;
        Token name = requireIdent("expected a class name");
        List<NotchField> headerFields = new ArrayList<>();
        if (take("(")) {
            headerFields = parseParamList();
            require(")", "expected ')' to close the class header");
        }
        List<NotchField> bodyFields = new ArrayList<>();
        List<NotchFunctionDeclaration> methods = new ArrayList<>();
        while (!atEnd() && !peekKeyword("end")) {
            if (peekKeyword("field")) {
                bodyFields.add(parseFieldDeclaration());
            } else if (peekKeyword("function")) {
                methods.add((NotchFunctionDeclaration) parseFunctionDeclaration());
            } else {
                final var diag = new Diagnostic()
                        .note("expected a 'field' or 'function' declaration in the class body")
                        .highlight(currentToken());
                throw new ParseException(diag);
            }
        }
        requireKeyword("end", "unterminated class, expected 'end'");
        final var span = new Span(source(), start, lastToken().end());
        return new NotchClassDeclaration(span, name, headerFields, bodyFields, methods);
    }

    private NotchField parseFieldDeclaration() {
        if (!takeKeyword("field")) return null;
        Token name = requireIdent("expected a field name");
        QualifiedIdent type = null;
        if (take(":")) {
            type = requireQualifiedIdent("expected a field type after ':'");
        }
        NotchExpression initializer = null;
        if (take("=")) {
            initializer = requireExpression("expected an initializer expression after '='");
        }
        return new NotchField(name, type, initializer);
    }

    private NotchStatement parseAssignmentStatement() {
        if (!peek("ident") && !peekKeyword("this")) return null;
        int assignmentStart = tokens.index;
        var start = tokens.location();

        NotchExpression base = new NotchIdentifier(take());
        Token lastProp = null;
        while (peek(".")) {
            take(".");
            if (lastProp != null) {
                base = new NotchPropertyAccess(base, lastProp);
            }
            lastProp = requireIdent("expected a property name");
        }

        if (!peek("=")) {
            tokens.index = assignmentStart;
            return null;
        }
        require("=", "expected '='");
        NotchExpression value = requireExpression("expected an expression after '='");
        final var span = new Span(source(), start, lastToken().end());

        if (lastProp == null && base instanceof NotchIdentifier id) {
            Token varName = id.token;
            if (varName.str().equals("this")) {
                final var diag = new Diagnostic()
                        .note("cannot assign to 'this' it always refers to the current object and can't be reassigned")
                        .highlight(varName.span());
                throw new ParseException(diag);
            }
            return new NotchAssignment(varName, value);
        }
        return new NotchPropertyAssignment(span, base, lastProp, value);
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
        if (takeKeyword("print")) {
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
