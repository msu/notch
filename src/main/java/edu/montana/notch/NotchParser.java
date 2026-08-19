package edu.montana.notch;

import edu.montana.notch.chisel.*;
import edu.montana.notch.expressions.*;
import edu.montana.notch.statements.*;
import edu.montana.notch.templates.ast.QualifiedIdent;
import edu.montana.notch.util.ParserErrorHandler;

import java.util.*;
import java.util.function.Supplier;

import static edu.montana.notch.util.Exceptions.rethrow;
import static edu.montana.notch.util.Text.repr;

public class NotchParser extends BasicParser {
    private final ParserErrorHandler errorHandler = new ParserErrorHandler(this);
    private int loopDepth = 0;
    private int functionDepth = 0;
    private int closureBodyDepth = 0;
    private int catchDepth = 0;
    private final List<Diagnostic> parseErrors = new ArrayList<>();

    public boolean inReturnableContext() {
        return functionDepth > 0;
    }

    public boolean inClosureBody() {
        return closureBodyDepth > 0;
    }

    public List<Diagnostic> getDiagnostics() {
        return Collections.unmodifiableList(parseErrors);
    }

    public boolean hasErrors() {
        return !parseErrors.isEmpty();
    }

    private static final Set<String> SYNC_KEYWORDS = Set.of(
            "if", "for", "repeat", "function", "print",
            "try", "throw", "return", "rethrow", "class",
            "break", "continue",
            "end", "catch", "else"
    );

    public NotchParser(TokenStream tokens) {
        super(tokens);
        ignoredTokenTypes.add("_ws");
        ignoredTokenTypes.add("_comment");
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
            throw errorHandler.catchInRecoverExpression();
        }

        if (!peekKeyword("recover")) return tryExpr;

        List<NotchRecoverExpression.TypedRecover> typedRecovers = new ArrayList<>();
        NotchExpression untypedRecover = null;
        while (!atEnd() && takeKeyword("recover")) {
            if (!recoverClauseIsTyped()) {
                takeIdent("with");
                untypedRecover = requireExpression(errorHandler::expectedExpressionAfterRecover);
                break;
            }
            typedRecovers.add(parseTypedRecover());
        }
        return new NotchRecoverExpression(tryExpr.span.through(lastToken()), tryExpr, typedRecovers, untypedRecover);
    }

    private boolean recoverClauseIsTyped() {
        if (takeIdent("from")) return true;
        if (!peek("ident") || peekIdent("with")) return false;
        Token typeName = peek();
        Token following = peekNext();
        final boolean somethingFollows = !following.type.equals("eoi");
        final boolean onTheSameLine = following.startLine() == typeName.endLine();
        final boolean startsACall = following.type.equals("(");
        return somethingFollows && onTheSameLine && !startsACall;
    }

    private NotchRecoverExpression.TypedRecover parseTypedRecover() {
        QualifiedIdent type = parseQualifiedIdent();
        takeIdent("with");
        NotchExpression expr = parseConditionalExpr();
        if (expr == null) throw errorHandler.expectedExpressionAfterRecoverType();
        return new NotchRecoverExpression.TypedRecover(type, expr);
    }

    //TODO: Currently gives uncoded errors where used in the parser
    public NotchExpression requireExpression(String errorMessage) {
        try {
            NotchExpression expr = parseExpression();
            if (expr == null) {
                final var diag = new Diagnostic()
                        .note(errorMessage)
                        .highlight(tokens.peek());
                parseErrors.add(diag);
                return new NotchErrorExpression(tokens.peek().span(), diag);
            }
            return expr;
        } catch (ParseException e) {
            parseErrors.add(e.diagnostic);
            return new NotchErrorExpression(tokens.peek().span(), e.diagnostic);
        }
    }

    public NotchExpression requireExpression(Supplier<ParseException> error) {
        try {
            NotchExpression expr = parseExpression();
            if (expr == null) {
                final var diag = error.get().diagnostic;
                parseErrors.add(diag);
                return new NotchErrorExpression(tokens.peek().span(), diag);
            }
            return expr;
        } catch (ParseException e) {
            parseErrors.add(e.diagnostic);
            return new NotchErrorExpression(tokens.peek().span(), e.diagnostic);
        }
    }

    private NotchExpression parseConditionalExpr() {
        var expr = parseFallbackExpr();
        if (expr == null) return null;

        // if the next keyword is an 'if' on the same line, it applies to the expressions
        if (takeKeywordOnSameLine("if", expr)) {
            var condition = parseFallbackExpr();
            if (condition == null) {
                throw errorHandler.expectedConditionAfterIfOperator();
            }

            NotchExpression fallback = null;
            if (takeKeywordOnSameLine("else", expr)) {
                fallback = parseConditionalExpr();
                if (fallback == null) {
                    throw errorHandler.expectedValueAfterElseOperator();
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
        var expr = parseLogicalOrExpression();
        if (expr == null) return null;

        while (take("?:")) {
            var fallback = parseLogicalOrExpression();
            if (fallback == null) {
                throw errorHandler.expectedExpressionAfterOperator("?:");
            }
            expr = new NotchFallbackExpression(expr, fallback);
        }

        return expr;
    }

    private NotchExpression parseLogicalOrExpression() {
        var expr = parseLogicalAndExpression();
        if (expr == null) return null;

        while (take("||") || takeIdent("or")) {
            final var op = lastToken();
            var rhs = parseLogicalAndExpression();
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

    private NotchExpression parseLogicalAndExpression() {
        NotchExpression expr = parseEqualityExpr();
        if (expr == null) return null;

        while (take("&&") || takeIdent("and")) {
            final var op = lastToken();
            var rhs = parseEqualityExpr();
            if (rhs == null) {
                throw errorHandler.expectedExpressionAfterOperator(op.str());
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
                throw errorHandler.expectedExpressionAfterOperator(op.str());
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
                    throw errorHandler.expectedExpressionAfterOperator(op.str());
                }
                expr = new NotchComparisonExpression(op, expr, rhs);
            } else if (takeIdent("starts")) {
                if (!takeIdent("with")) throw errorHandler.expectedWithAfter("starts");
                var rhs = parseAdditiveExpression();
                if (rhs == null) {
                    throw errorHandler.expectedExpressionAfterOperator("starts with");
                }
                expr = new NotchStartsWithExpression(expr, rhs);
            } else if (takeIdent("ends")) {
                if (!takeIdent("with")) throw errorHandler.expectedWithAfter("ends");
                var rhs = parseAdditiveExpression();
                if (rhs == null) {
                    throw errorHandler.expectedExpressionAfterOperator("ends with");
                }
                expr = new NotchEndsWithExpression(expr, rhs);
            } else if (takeIdent("contains")) {
                var rhs = parseAdditiveExpression();
                if (rhs == null) {
                    throw errorHandler.expectedExpressionAfterOperator("contains");
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
                throw errorHandler.expectedExpressionAfterOperator(opToken.str());
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
                throw errorHandler.expectedExpressionAfterOperator(opToken.str());
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
            return parseNotNullExpression();
        }
    }

    private NotchExpression parseNotNullExpression() {
        NotchExpression expr = parseIndirectExpression();
        if (expr != null && take("!")) {
            return new NotchNotNullExpression(expr, lastToken());
        }
        return expr;
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
            var beforeEmpty = checkpoint();
            tokens.take();
            if (!peek("(") && !peek(".")) {
                return new NotchIsEmptyExpression(lhs, isInverted, tokens.prev());
            }
            rollbackTo(beforeEmpty);
        }
        if (peekIdent("null")) {
            int beforeEmpty = tokens.index;
            tokens.take();
            if (!peek("(") && !peek(".")) {
                return new NotchIsNullExpression(lhs, isInverted, tokens.prev());
            }
            tokens.index = beforeEmpty;
        }
        if (peekIdent("undefined")) {
            int beforeEmpty = tokens.index;
            tokens.take();
            if (!peek("(") && !peek(".")) {
                return new NotchIsUndefinedExpression(lhs, isInverted, tokens.prev());
            }
            tokens.index = beforeEmpty;
        }
        NotchExpression rhs = parseComparisonExpression();
        if (rhs == null) {
            throw errorHandler.expectedExpressionAfterOperator(isInverted ? "is not" : "is");
        }
        String opStr = isInverted ? "!=" : "==";
        Token Op = new Token(isToken.span(), opStr, opStr);
        return new NotchEquality(Op, lhs, rhs);
    }

    private NotchExpression parseIndexOperation(NotchExpression root) {
        if (take("[")) {
            NotchExpression value = requireExpression(errorHandler::expectedIndexExpression);
            if (!take("]")) throw errorHandler.expectedCloseBracketForIndex();
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
                        throw errorHandler.expectedCommaBetweenArguments();
                    }
                }
            }
            if (!take(")")) {
                throw errorHandler.expectedCloseParenForArguments();
            }
            NotchMethodInvocationExpression methodInvocation = new NotchMethodInvocationExpression(root, args, lastToken().end());
            if (root instanceof NotchPropertyAccessExpression pa) {
                pa.setFavorMethods(true);
            }
            return methodInvocation;
        }
        return null;
    }

    private NotchExpression parsePropertyAccessExpression(NotchExpression root) {
        if (take(".")) {
            if (peek("keyword")) {
                throw errorHandler.keywordAsPropertyName();
            }
            if (!peek("ident")) throw errorHandler.expectedPropertyName();
            Token propName = take();
            NotchPropertyAccessExpression propAccess = new NotchPropertyAccessExpression(root, propName);
            return propAccess;
        }
        return null;
    }

    private NotchExpression parsePrimaryExpression() {
        Token paren = consume("(");
        if (paren != null) {
            NotchExpression expr = parseExpression();
            if (!take(")")) {
                throw errorHandler.expectedCloseParenForGrouping(paren);
            }
            final var span = expr.span.through(lastToken());
            NotchParenthesizedExpression parenExpr = new NotchParenthesizedExpression(span, expr);
            return parenExpr;
        }

        Token bool = consume("bool");
        if (bool != null) {
            return new NotchBooleanExpression(bool);
        }

        Token word = consume("ident");
        if (word != null) {
            return new NotchIdentifierExpression(word, !take("?"), lastToken());
        }

        Token intToken = consume("int");
        if (intToken != null) {
            return new NotchIntegerExpression(intToken);
        }

        Token stringToken = consume("string");
        if (stringToken != null) {
            return new NotchStringExpression(stringToken);
        }

        Token fStringToken = consume("fstring");
        if (fStringToken != null) {
            return new NotchFStringExpression(fStringToken);
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
            return new NotchIdentifierExpression(take());
        }

        if (peekKeyword("null")) {
            return new NotchNullExpression(take());
        }

        return null;
    }

    private NotchExpression parseInstantiation() {
        var start = tokens.location();
        if (!takeKeyword("new")) return null;
        if (!peek("ident")) throw errorHandler.expectedClassNameAfterNew();
        Token className = take();
        if (!take("(")) throw errorHandler.expectedOpenParenAfterClassName();
        List<NotchExpression> args = parseArgList();
        if (!take(")")) throw errorHandler.expectedCloseParenForArguments();
        final var span = new Span(source(), start, lastToken().end());
        return new NotchInstantiationExpression(span, className, args);
    }

    private NotchExpression parseListLiteral() {
        Token start = currentToken();
        if (take("[")) {
            List<NotchExpression> listValues = new LinkedList<>();
            while (!atEnd() && !peek("]")) {
                NotchExpression notchExpression = parseExpression();
                listValues.add(notchExpression);
                if (!peek("]")) {
                    if (!take(",")) throw errorHandler.expectedCommaBetweenListElements();
                } else {
                    take(","); // allow a trailing comma
                }
            }
            if (!take("]")) throw errorHandler.expectedCloseBracketForList();
            NotchListLiteralExpression notchListLiteral = new NotchListLiteralExpression(start.span.through(lastToken()), listValues);
            return notchListLiteral;
        }
        return null;
    }

    private NotchExpression parseBraceLiteral() {
        Span start = currentToken().span;
        if (!take("{")) return null;
        if (take("}")) {
            return new NotchMapLiteralExpression(start.through(lastToken()), new ArrayList<>(), new ArrayList<>());
        }
        if (take(",")) {
            if (!take("}")) throw errorHandler.expectedCloseBraceFor("empty set");
            return new NotchSetLiteralExpression(start.through(lastToken()), new ArrayList<>());
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
                if (!take("->")) throw errorHandler.expectedArrowBetweenMapKeyAndValue();
                values.add(parseExpression());
            }
            if (!take("}")) throw errorHandler.expectedCloseBraceFor("map");
            return new NotchMapLiteralExpression(start.through(lastToken()), keys, values);
        } else {
            List<NotchExpression> values = new ArrayList<>();
            values.add(first);
            while (take(",")) {
                if (peek("}")) break;
                values.add(parseExpression());
            }
            if (!take("}")) throw errorHandler.expectedCloseBraceFor("set");
            return new NotchSetLiteralExpression(start.through(lastToken()), values);
        }
    }

    private NotchExpression parseClosureExpression() {
        if (!take("\\")) return null;
        Token startToken = lastToken();
        List<Token> params = new ArrayList<>();
        while (!atEnd() && !peek("->")) {
            if (!peek("ident")) throw errorHandler.expectedParameterName();
            Token param = take();
            params.add(param);
            if (!peek("->")) {
                if (!take(",")) throw errorHandler.expectedCommaBetweenParameters();
            }
        }
        if (!take("->")) throw errorHandler.expectedArrowAfterClosureParameters();
        List<NotchStatement> statements = null;
        NotchExpression expression = null;
        if (take("{")) {
            statements = new LinkedList<>();
            int savedLoopDepth = loopDepth;
            loopDepth = 0;
            functionDepth++;
            closureBodyDepth++;
            try {
                while (!atEnd() && !peek("}")) {
                    NotchStatement stmt = parseStatement();
                    statements.add(stmt);
                }
            } finally {
                functionDepth--;
                closureBodyDepth--;
                loopDepth = savedLoopDepth;
            }
            if (!take("}")) throw errorHandler.expectedCloseBraceForClosureBody();
        } else {
            expression = parseExpression();
        }
        final var span = startToken.span.through(lastToken());
        NotchClosureExpression closureExpr = new NotchClosureExpression(span, params, expression, statements);
        return closureExpr;
    }

    public NotchElement parse() {
        NotchExpression notchExpression;
        boolean tokensAfterExpr;
        Exception expressionException = null;
        var mark = checkpoint();
        try {
            notchExpression = parseExpression();
            tokensAfterExpr = !tokens.atEnd();
            if (notchExpression != null && !tokensAfterExpr) {
                return notchExpression;
            }
        } catch (Exception e) {
            expressionException = e;
        }
        rollbackTo(mark);
        NotchStatement notchStatement;
        try {
            notchStatement = parseAsStatement();
            return notchStatement;
        } catch (Exception e) {
            if (expressionException != null) {
                throw rethrow(expressionException);
            } else {
                throw rethrow(e);
            }
        }
    }

    private record Checkpoint(int tokenIndex, int errorCount) {}

    private Checkpoint checkpoint() {
        return new Checkpoint(tokens.index, parseErrors.size());
    }

    private void rollbackTo(Checkpoint checkpoint) {
        tokens.index = checkpoint.tokenIndex();
        parseErrors.subList(checkpoint.errorCount(), parseErrors.size()).clear();
    }

    private boolean advancedFrom(Checkpoint checkpoint) {
        return tokens.index > checkpoint.tokenIndex();
    }

    private boolean isSyncToken() {
        return peek("keyword") && SYNC_KEYWORDS.contains(currentToken().str());
    }

    private void synchronize() {
        while (!atEnd()) {
            if (isSyncToken()) break;
            take();
        }
    }

    public NotchStatement parseAsStatement() {
        var stmts = new ArrayList<NotchStatement>();
        var start = currentToken();
        while (!atEnd()) {
            stmts.add(parseStatement());
        }
        if (stmts.size() == 1) {
            return stmts.getFirst();
        } else {
            return new NotchStatements(start.span.through(currentToken()), stmts);
        }
    }

    private NotchStatement parseStatement() {
        Span errorSpan = currentToken().span();
        var mark = checkpoint();
        try {
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
            var importStmt = parseImportStatement();
            if (importStmt != null) {
                return importStmt;
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

            throw errorHandler.expectedStatement();
        } catch (ParseException e) {
            parseErrors.add(e.diagnostic);
            if (!advancedFrom(mark)) take();
            synchronize();
            return new NotchErrorStatement(errorSpan, e.diagnostic);
        }
    }

    private NotchStatement parseCallStatement() {
        var mark = checkpoint();
        NotchExpression expr;
        try {
            expr = parseExpression();
        } catch (ParseException e) {
            if (advancedFrom(mark)) throw e;
            expr = null;
        }
        if (expr == null) {
            rollbackTo(mark);
            return null;
        }
        if (peek("=")) {
            throw errorHandler.cannotAssignToThisExpression(expr);
        }
        if (expr instanceof NotchMethodInvocationExpression || expr instanceof NotchRecoverExpression) {
            return new NotchExpressionStatement(expr);
        }
        throw errorHandler.cannotBeUsedAsAStatement(expr);
    }

    private NotchStatement parseThrowStatement() {
        var start = tokens.location();
        if (!takeKeyword("throw")) return null;
        Token throwToken = lastToken();
        if (atEnd() || currentToken().startLine() != throwToken.endLine()) {
            throw errorHandler.expectedExpressionAfterThrow();
        }
        NotchExpression operand = requireExpression(errorHandler::expectedExpressionToThrow);
        final var span = new Span(source(), start, lastToken().end());
        return new NotchThrowStatement(span, operand);
    }

    private NotchStatement parseImportStatement() {
        var start = tokens.location();
        if (!takeKeyword("import")) return null;

        QualifiedIdent type = parseQualifiedIdent();
        if (type == null) throw errorHandler.expectedTypeNameAfterImport();

        Token alias = null;
        if (takeKeyword("as")) {
            if (!peek("ident")) throw errorHandler.expectedAliasAfterAs();
            alias = take();
        }

        final var span = new Span(source(), start, lastToken().end());
        return new NotchImport(span, type, alias);
    }

    private NotchStatement parseRethrowStatement() {
        var start = tokens.location();
        if (!takeKeyword("rethrow")) return null;
        final var span = new Span(source(), start, lastToken().end());
        if (catchDepth == 0) {
            throw errorHandler.rethrowOutsideCatch(span);
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
                    if (!peek("ident")) throw errorHandler.expectedBindingNameAfterAs();
                    exceptionName = take();
                } else if (!atEnd() && currentToken().startLine() == catchTokenLine) {
                    throw errorHandler.catchBodyMustStartOnNewLine();
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

        if (!takeKeyword("end")) throw errorHandler.expectedEndOfBlock("try block");
        final var span = new Span(source(), start, lastToken().end());
        return new NotchTry(span, body, clauses);
    }

    private NotchStatement parseRepeatStatement() {
        var start = tokens.location();
        if (!takeKeyword("repeat")) return null;
        if (takeKeyword("while")) {
            NotchExpression cond = requireExpression(() -> errorHandler.expectedConditionAfter("while"));
            List<NotchStatement> body = parseLoopBody();
            if (!takeKeyword("end")) throw errorHandler.expectedEndOfBlock("repeat statement");
            final var span = new Span(source(), start, lastToken().end());
            return new NotchRepeatWhile(span, cond, body);
        }
        if (takeKeyword("until")) {
            NotchExpression cond = requireExpression(() -> errorHandler.expectedConditionAfter("until"));
            List<NotchStatement> body = parseLoopBody();
            if (!takeKeyword("end")) throw errorHandler.expectedEndOfBlock("repeat statement");
            final var span = new Span(source(), start, lastToken().end());
            return new NotchRepeatUntil(span, cond, body);
        }

        NotchExpression count = requireExpression(errorHandler::expectedCountExpressionAfterRepeat);
        if (!takeKeyword("times")) {
            throw errorHandler.expectedTimesAfterRepeatCount();
        }
        List<NotchStatement> body = parseLoopBody();
        if (!takeKeyword("end")) throw errorHandler.expectedEndOfBlock("repeat statement");
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
            throw errorHandler.keywordOutsideLoop(span, keyword);
        }
    }

    private void requireInFunction(Span span, String keyword) {
        if (functionDepth == 0) {
            throw errorHandler.keywordOutsideFunction(span, keyword);
        }
    }

    private List<NotchField> parseParamList() {
        List<NotchField> params = new ArrayList<>();
        while (!atEnd() && !peek(")")) {
            if (!peek("ident")) throw errorHandler.expectedParameterName();
            Token name = take();
            QualifiedIdent type = null;
            if (take(":")) {
                type = parseQualifiedIdent();
                if (type == null) throw errorHandler.expectedTypeAfterColon("parameter");
            }
            params.add(new NotchField(name, type, null));
            if (!peek(")")) {
                if (!take(",")) throw errorHandler.expectedCommaBetweenParameters();
            }
        }
        return params;
    }

    private List<NotchExpression> parseArgList() {
        List<NotchExpression> args = new ArrayList<>();
        while (!atEnd() && !peek(")")) {
            args.add(parseExpression());
            if (!peek(")")) {
                if (!take(",")) throw errorHandler.expectedCommaBetweenArguments();
            }
        }
        return args;
    }

    private NotchStatement parseFunctionDeclaration() {
        var start = tokens.location();
        if (!takeKeyword("function")) return null;
        if (!peek("ident")) throw errorHandler.expectedFunctionName();
        Token name = take();
        if (!take("(")) throw errorHandler.expectedOpenParenAfterFunctionName();
        List<NotchField> params = parseParamList();
        if (!take(")")) throw errorHandler.expectedCloseParenFor("parameter list");
        QualifiedIdent returnType = null;
        if (take(":")) {
            returnType = parseQualifiedIdent();
            if (returnType == null) throw errorHandler.expectedTypeAfterColon("return");
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
            if (!takeKeyword("end")) throw errorHandler.expectedEndOfBlock("function");
        }
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
        if (!peek("ident")) throw errorHandler.expectedClassName();
        Token name = take();
        List<NotchField> headerFields = new ArrayList<>();
        if (take("(")) {
            headerFields = parseParamList();
            if (!take(")")) throw errorHandler.expectedCloseParenFor("class header");
        }
        List<NotchField> bodyFields = new ArrayList<>();
        List<NotchFunctionDeclaration> methods = new ArrayList<>();
        while (!atEnd() && !peekKeyword("end")) {
            try {
                if (peekKeyword("field")) {
                    bodyFields.add(parseFieldDeclaration());
                } else if (peekKeyword("function")) {
                    methods.add((NotchFunctionDeclaration) parseFunctionDeclaration());
                } else {
                    throw errorHandler.expectedFieldOrFunctionInClassBody();
                }
            } catch (ParseException e) {
                parseErrors.add(e.diagnostic);
                //tighter context error synchronization for class
                while (!atEnd() && !peekKeyword("field", "function", "end")) {
                    take();
                }
            }
        }
        if (!takeKeyword("end")) throw errorHandler.expectedEndOfBlock("class");
        final var span = new Span(source(), start, lastToken().end());
        return new NotchClassDeclaration(span, name, headerFields, bodyFields, methods);
    }

    private NotchField parseFieldDeclaration() {
        if (!takeKeyword("field")) return null;
        if (!peek("ident")) throw errorHandler.expectedFieldName();
        Token name = take();
        QualifiedIdent type = null;
        if (take(":")) {
            type = parseQualifiedIdent();
            if (type == null) throw errorHandler.expectedTypeAfterColon("field");
        }
        NotchExpression initializer = null;
        if (take("=")) {
            initializer = requireExpression(errorHandler::expectedInitializerExpressionAfterEquals);
        }
        return new NotchField(name, type, initializer);
    }

    private NotchStatement parseAssignmentStatement() {
        if (!peek("ident") && !peekKeyword("this")) return null;
        var mark = checkpoint();
        var start = tokens.location();

        NotchExpression target = parseAssignmentTarget();

        if (!take("=")) { //a.b[0].run()
            rollbackTo(mark);
            return null;
        }
        NotchExpression value = requireExpression(errorHandler::expectedExpressionAfterEquals);
        final var span = new Span(source(), start, lastToken().end());
        return buildAssignmentStatement(span, target, value);
    }

    private NotchExpression parseAssignmentTarget() {
        NotchExpression target = new NotchIdentifierExpression(take());
        while (peek(".") || peek("[")) {
            if (take(".")) {
                if (peek("keyword")) throw errorHandler.keywordAsPropertyName();
                if (!peek("ident")) throw errorHandler.expectedPropertyName();
                Token property = take();
                target = new NotchPropertyAccessExpression(target, property);
            } else {
                take("[");
                NotchExpression index = requireExpression(errorHandler::expectedIndexExpression);
                if (!take("]")) throw errorHandler.expectedCloseBracketForIndex();
                target = new NotchIndexExpression(target, index, lastToken().end());
            }
        }
        return target;
    }

    private NotchStatement buildAssignmentStatement(Span span, NotchExpression target, NotchExpression value) {
        if (target instanceof NotchIndexExpression index) {
            return new NotchIndexAssignment(span, index.root, index.index, value);
        }
        if (target instanceof NotchPropertyAccessExpression propertyAccess) {
            return new NotchPropertyAssignment(span, propertyAccess.getRoot(), propertyAccess.getPropertyToken(), value);
        }
        Token varName = ((NotchIdentifierExpression) target).token;
        if (varName.str().equals("this")) {
            throw errorHandler.cannotAssignToThis(varName);
        }
        return new NotchAssignment(varName, value);
    }

    private NotchForLoop parseForStatement() {
        var start = tokens.location();
        if (takeKeyword("for")) {
            if (peek("keyword")) {
                throw errorHandler.keywordAsLoopVariable();
            }
            if (!peek("ident")) throw errorHandler.expectedLoopItemName();
            Token loopIdentifier = take();
            if (!takeKeyword("in")) throw errorHandler.expectedInAfterLoopVariable();

            NotchExpression loopExpression = requireExpression(errorHandler::expectedExpressionForLoopIterable);

            Token indexIdentifier = null;
            if (takeIdent("index")) {
                if (!peek("ident")) throw errorHandler.expectedLoopIndexName();
                indexIdentifier = take();
            }

            List<NotchStatement> loopBodyStatements = parseLoopBody();

            if (!takeKeyword("end")) throw errorHandler.expectedEndOfBlock("for statement");

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
                throw errorHandler.expectedConditionalAfterIf(lastToken());
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

            if (!takeKeyword("end")) throw errorHandler.expectedEndOfBlock("if statement");

            final var span = new Span(source(), start, lastToken().end());
            NotchIf notchIf = new NotchIf(span, conditional, ifTrue, ifFalse);
            return notchIf;
        }
        return null;
    }

    private NotchPrint parsePrintStatement() {
        var start = tokens.location();
        if (takeKeyword("print")) {
            var printToken = lastToken();
            if (!take("(")) {
                throw errorHandler.expectedParenAfterPrint(printToken);
            }
            NotchExpression expr = parseExpression();
            if (!take(")")) {
                throw errorHandler.expectedCloseParenForPrint();
            }
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
