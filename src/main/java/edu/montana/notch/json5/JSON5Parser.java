package edu.montana.notch.json5;

import edu.montana.notch.chisel.*;
import edu.montana.notch.util.JSON5;

public class JSON5Parser extends BasicParser {
    {
        ignoredTokenTypes.add("_ws");
        ignoredTokenTypes.add("_comment");
    }

    public JSON5Parser(Source source) {
        this(JSON5.tokenize(source));
    }

    public JSON5Parser(TokenStream tokens) {
        super(tokens);
    }

    public JSON5Value parseValue() {
        var s = parseString();
        if (s != null) {
            return s;
        }

        var n = parseNumber();
        if (n != null) {
            return n;
        }

        var b = parseBoolean();
        if (b != null) {
            return b;
        }

        var nv = parseNull();
        if (nv != null) {
            return nv;
        }

        var arr = parseArray();
        if (arr != null) {
            return arr;
        }

        var obj = parseObject();
        if (obj != null) {
            return obj;
        }

        return null;
    }

    private String parseMemberName() {
        if (peek("string")) {
            var token = take();
            return ((JSON5TokenTypeString.StringValue) token.data).value();
        }

        if (peek("ident")) {
            var token = take();
            return token.str();
        }

        return null;
    }

    public JSON5Object parseObject() {
        if (!take("{")) return null;

        var out = new JSON5Object();

        while (!atEnd() && !peek("}")) {
            var key = parseMemberName();
            if (key == null) {
                final var diag = new Diagnostic()
                        .highlight(currentToken())
                        .note("expected a member name (string or identifier) in json-object");
                throw new ParseException(diag);
            }

            if (!take(":")) {
                final var diag = new Diagnostic()
                        .highlight(currentToken())
                        .note("expected a ':' character after json-object member name");
                throw new ParseException(diag);
            }

            var value = parseValue();
            if (value == null) {
                final var diag = new Diagnostic()
                        .highlight(currentToken())
                        .note("expected a json value after json-object member");
                throw new ParseException(diag);
            }

            out.put(key, value);

            if (!take(",")) {
                break;
            }
        }

        var start = currentToken();
        if (parseMemberName() != null) {
            final var diag = new Diagnostic()
                    .highlight(start)
                    .note("expected ',' or '}' in json-object, did you for get a comma??");
            throw new ParseException(diag);
        }

        if (!take("}")) {
            final var diag = new Diagnostic()
                    .highlight(currentToken())
                    .note("expected '}' at the end of this json-object");
            throw new ParseException(diag);
        }

        return out;
    }

    public JSON5Array parseArray() {
        if (!take("[")) return null;

        var out = new JSON5Array();

        while (!atEnd() && !peek("]")) {
            var value = parseValue();
            if (value == null) {
                final var diag = new Diagnostic();
                diag.note("expected a json value in json-array");
                diag.highlight(currentToken());
                throw new ParseException(diag);
            }

            out.add(value);

            if (!take(",")) {
                break;
            }
        }

        var start = currentToken();
        if (peekValue()) {
            final var diag = new Diagnostic();
            diag.highlight(start);
            diag.note("expected ',' or ']' in json-array, did you forget a comma??");
            throw new ParseException(diag);
        }

        if (!take("]")) {
            final var diag = new Diagnostic();
            diag.highlight(currentToken());
            diag.note("expected ']' at the end of this json-array");
            throw new ParseException(diag);
        }

        return out;
    }

    public boolean peekValue() {
        return tokens.peek("null", "ident", "{", "[", "num", "string");
    }

    public JSON5Null parseNull() {
        if (take("null")) return JSON5Null.NULL;
        return null;
    }

    public JSON5Boolean parseBoolean() {
        if (take("true")) return JSON5Boolean.TRUE;
        if (take("false")) return JSON5Boolean.FALSE;
        return null;
    }

    public JSON5String parseString() {
        if (!peek("string")) {
            return null;
        }

        var content = take();
        var value = ((JSON5TokenTypeString.StringValue) content.data);
        return new JSON5String(value.value());
    }

    public JSON5Number parseNumber() {
        if (!peek("num")) {
            return null;
        }

        var content = take();
        var value = ((JSON5TokenTypeNumber.NumberValue) content.data);
        if (value.isInteger()) return new JSON5Integer(value.integerValue());
        return new JSON5Decimal(value.decimalValue());
    }

    public JSON5Integer parseInteger() {
        if (!peek("num")) {
            return null;
        }

        var content = currentToken();
        var value = ((JSON5TokenTypeNumber.NumberValue) content.data);
        if (!value.isInteger()) {
            return null;
        }

        take();
        return new JSON5Integer(value.integerValue());
    }

    public JSON5Decimal parseDecimal() {
        if (!peek("num")) {
            return null;
        }

        var content = currentToken();
        var value = ((JSON5TokenTypeNumber.NumberValue) content.data);
        if (value.isInteger()) {
            return null;
        }

        take();
        return new JSON5Decimal(value.decimalValue());
    }
}

