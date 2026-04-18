package bigsky.notch.json5;

import bigsky.notch.util.JSON5;
import bigsky.notch.chisel.BasicParser;
import bigsky.notch.chisel.ParseException;
import bigsky.notch.chisel.TokenStream;

import static bigsky.notch.json5.JSON5TokenTypeIdent.JSON5_IDENT;
import static bigsky.notch.json5.JSON5TokenTypeIdent.Keyword.*;
import static bigsky.notch.json5.JSON5TokenTypePunct.*;
import static bigsky.notch.json5.JSON5TokenTypeString.JSON5_STRING;
import static bigsky.notch.json5.JSON5TokenTypeNumber.JSON5_NUMBER;

public class JSON5Parser extends BasicParser {
    {
        ignoredTokenTypes.add(JSON5TokenTypeWhitespace.JSON5_WHITESPACE);
        ignoredTokenTypes.add(JSON5TokenTypeComment.JSON5_COMMENT);
    }

    public JSON5Parser(String fileId, String source) {
        this(JSON5.tokenize(fileId, source));
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
        if (peek(JSON5_STRING)) {
            var token = take();
            return ((JSON5TokenTypeString.StringValue) token.data).value();
        }

        if (peek(JSON5_IDENT)) {
            var token = take();
            return token.str();
        }

        return null;
    }

    public JSON5Object parseObject() {
        if (!take(LeftBrace)) return null;

        var out = new JSON5Object();

        while (!atEnd() && !peek(RightBrace)) {
            var key = parseMemberName();
            if (key == null) {
                throw new ParseException(fileId(), "expected a member name (string or identifier) in json-object", peek().span());
            }

            if (!take(Colon)) {
                throw new ParseException(fileId(), "expected a ':' character after json-object member name", peek().span());
            }

            var value = parseValue();
            if (value == null) {
                throw new ParseException(fileId(), "expected a json value after json-object member", peek().span());
            }

            out.put(key, value);

            if (!take(Comma)) {
                break;
            }
        }

        var start = peek();
        if (parseMemberName() != null) {
            throw new ParseException(fileId(), "expected ',' or '}' in json-object, did you for get a comma??", start.span());
        }

        if (!take(RightBrace)) {
            throw new ParseException(fileId(), "expected '}' at the end of this json-object", peek().span());
        }

        return out;
    }

    public JSON5Array parseArray() {
        if (!take(LeftBracket)) return null;

        var out = new JSON5Array();

        while (!atEnd() && !peek(RightBracket)) {
            var value = parseValue();
            if (value == null) {
                throw new ParseException(fileId(), "expected a json value in json-array", peek().span());
            }

            out.add(value);

            if (!take(Comma)) {
                break;
            }
        }

        var start = peek();
        if (peekValue()) {
            throw new ParseException(fileId(), "expected ',' or ']' in json-array, did you forget a comma??", start.span());
        }

        if (!take(RightBracket)) {
            throw new ParseException(fileId(), "expected ']' at the end of this json-array", peek().span());
        }

        return out;
    }

    public boolean peekValue() {
        return tokens.peek(JSON_NULL, JSON5_IDENT, LeftBrace, LeftBracket, JSON5_NUMBER, JSON5_STRING);
    }

    public JSON5Null parseNull() {
        if (take(JSON_NULL)) return JSON5Null.NULL;
        return null;
    }

    public JSON5Boolean parseBoolean() {
        if (take(JSON_TRUE)) return JSON5Boolean.TRUE;
        if (take(JSON_FALSE)) return JSON5Boolean.FALSE;
        return null;
    }

    public JSON5String parseString() {
        if (!peek(JSON5_STRING)) {
            return null;
        }

        var content = take();
        var value = ((JSON5TokenTypeString.StringValue) content.data);
        return new JSON5String(value.value());
    }

    public JSON5Number parseNumber() {
        if (!peek(JSON5_NUMBER)) {
            return null;
        }

        var content = take();
        var value = ((JSON5TokenTypeNumber.NumberValue) content.data);
        if (value.isInteger()) return new JSON5Integer(value.integerValue());
        return new JSON5Decimal(value.decimalValue());
    }

    public JSON5Integer parseInteger() {
        if (!peek(JSON5_NUMBER)) {
            return null;
        }

        var content = peek();
        var value = ((JSON5TokenTypeNumber.NumberValue) content.data);
        if (!value.isInteger()) {
            return null;
        }

        take();
        return new JSON5Integer(value.integerValue());
    }

    public JSON5Decimal parseDecimal() {
        if (!peek(JSON5_NUMBER)) {
            return null;
        }

        var content = peek();
        var value = ((JSON5TokenTypeNumber.NumberValue) content.data);
        if (value.isInteger()) {
            return null;
        }

        take();
        return new JSON5Decimal(value.decimalValue());
    }
}

