package edu.montana.notch.json5.query;

import edu.montana.notch.chisel.*;
import edu.montana.notch.json5.JSON5TokenTypeNumber;
import edu.montana.notch.util.Text;

public class QueryParser extends BasicParser {
    public QueryParser(String src) {
        this(QueryEngine.tokenize(new Source("<query-expression>", src)));
    }

    public QueryParser(TokenStream tokenStream) {
        super(tokenStream);
    }

    public QueryExpression parseExpression() {
        return parseAccessExpression();
    }

    public QueryExpression parseAccessExpression() {
        var expr = parsePrimaryExpression();
        if (expr == null) return null;

        while (!atEnd()) {
            if (take(".")) {
                var field = requireField();
                expr = new FieldAccessExpression(expr, field);
                continue;
            }

            if (take("[")) {
                var index = requireIndex();
                var rbracket = require("]", "unterminated index expression");
                expr = new IndexExpression(expr, index, rbracket);
                continue;
            }

            break;
        }

        return expr;
    }

    public QueryExpression parsePrimaryExpression() {
        if (peek(".")) {
            QueryExpression expr = new RootExpression(take());
            if (peek("ident", "string")) {
                expr = new FieldAccessExpression(expr, requireField());
            }
            return expr;
        }

        return null;
    }

    private QueryExpression requireIndex() {
        if (peek("number")) {
            var token = take();
            var data = ((JSON5TokenTypeNumber.NumberValue) token.data);
            if (data.isInteger()) {
                var expr = new NumberExpression(token);
                return expr;
            }
        }

        var tt = currentToken().type;
        final var diag = new Diagnostic();
        diag.highlight(currentToken());
        diag.note("expected index: found %s token".formatted(Text.repr(tt)));
        throw new ParseException(diag);
    }

    private Token requireField() {
        if (peek("ident", "string")) {
            return take();
        }

        var tt = currentToken().type;
        final var diag = new Diagnostic();
        diag.highlight(currentToken());
        diag.note("expected field: found %s token".formatted(Text.repr(tt)));
        throw new ParseException(diag);
    }
}
