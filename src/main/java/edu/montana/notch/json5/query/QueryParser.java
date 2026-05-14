package edu.montana.notch.json5.query;

import edu.montana.notch.util.Text;
import edu.montana.notch.chisel.BasicParser;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenStream;
import edu.montana.notch.json5.JSON5TokenTypeNumber;

import static edu.montana.notch.chisel.type.TokenTypePunct.*;
import static edu.montana.notch.json5.JSON5TokenTypeString.JSON5_STRING;
import static edu.montana.notch.json5.JSON5TokenTypeNumber.JSON5_NUMBER;
import static edu.montana.notch.json5.JSON5TokenTypeIdent.JSON5_IDENT;

public class QueryParser extends BasicParser {
    public QueryParser(String src) {
        this(QueryEngine.tokenize("<query-expression>", src));
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
            if (take(DOT)) {
                var field = requireField();
                expr = new FieldAccessExpression(expr, field);
                continue;
            }

            if (take(LBRACKET)) {
                var index = requireIndex();
                var rbracket = require(RBRACKET, "unterminated index expression");
                expr = new IndexExpression(expr, index, rbracket);
                continue;
            }

            break;
        }

        return expr;
    }

    public QueryExpression parsePrimaryExpression() {
        if (peek(DOT)) {
            QueryExpression expr = new RootExpression(fileId(), take());
            if (peek(JSON5_IDENT, JSON5_STRING)) {
                expr = new FieldAccessExpression(expr, requireField());
            }
            return expr;
        }

        return null;
    }

    private QueryExpression requireIndex() {
        if (peek(JSON5_NUMBER)) {
            var token = take();
            var data = ((JSON5TokenTypeNumber.NumberValue) token.data);
            if (data.isInteger()) {
                var expr = new NumberExpression(fileId(), token);
                return expr;
            }
        }

        var tt = peek().type;
        throw new ParseException(fileId(), "expected index: found %s token".formatted(Text.repr(tt)), location());
    }

    private Token requireField() {
        if (peek(JSON5_IDENT, JSON5_STRING)) {
            return take();
        }

        var tt = peek().type;
        throw new ParseException(fileId(), "expected field: found %s token".formatted(Text.repr(tt)), location());
    }
}
