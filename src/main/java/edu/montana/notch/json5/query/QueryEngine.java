package edu.montana.notch.json5.query;

import edu.montana.notch.util.Text;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.TokenStream;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.type.TokenTypePunct;
import edu.montana.notch.json5.JSON5Boolean;
import edu.montana.notch.json5.JSON5Number;
import edu.montana.notch.json5.JSON5String;
import edu.montana.notch.json5.JSON5Value;

import static edu.montana.notch.json5.JSON5TokenTypeString.JSON5_STRING;
import static edu.montana.notch.json5.JSON5TokenTypeNumber.JSON5_NUMBER;
import static edu.montana.notch.json5.JSON5TokenTypeIdent.JSON5_IDENT;

public class QueryEngine {
    public static Tokenizer tokenizer() {
        return new Tokenizer()
                .withTokenType(JSON5_STRING)
                .withTokenType(JSON5_NUMBER)
                .withTokenType(JSON5_IDENT)
                .withTokenType(TokenTypePunct.DOT)
                .withTokenType(TokenTypePunct.LBRACKET)
                .withTokenType(TokenTypePunct.RBRACKET);
    }

    public static TokenStream tokenize(String fileId, String src) {
        return tokenizer().tokenize(fileId, src);
    }

    public static QueryExpression parseQueryExpression(String query) throws ParseException {
        var parser = new QueryParser(query);
        var expr = parser.parseExpression();
        if (expr == null) {
            throw new ParseException("invalid query string", parser.fileId(), parser.location());
        }
        return expr;
    }

    public static Object query(JSON5Value value, String query) {
        var expr = parseQueryExpression(query);
        var result = expr.query(value);
        if (result == QueryExpression.UNDEFINED) {
            throw new RuntimeException("query %s returned undefined".formatted(Text.repr(query)));
        }
        return result;
    }

    public static <T> T query(JSON5Value value, String path, Class<T> clazz) {
        var result = query(value, path);
        return cast(result, clazz);
    }

    public static boolean has(JSON5Value value, String query) {
        var expr = parseQueryExpression(query);
        var result = expr.query(value);
        return result != QueryExpression.UNDEFINED;
    }

    public static boolean has(JSON5Value value, String query, Class<?> clazz) {
        var expr = parseQueryExpression(query);
        var result = expr.query(value);
        if (result == QueryExpression.UNDEFINED) return false;
        try {
            cast(value, clazz);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> T cast(Object value, Class<T> clazz) {
        if (value instanceof JSON5String str && clazz.equals(String.class)) {
            return ((T) str.value);
        }
        if (value instanceof JSON5Number num) {
            if (clazz.equals(Integer.class)) return ((T) (Integer) num.value().intValue());
            if (clazz.equals(Long.class)) return ((T) (Long) num.value().longValue());
            if (clazz.equals(Short.class)) return ((T) (Short) num.value().shortValue());
            if (clazz.equals(Byte.class)) return ((T) (Byte) num.value().byteValue());
            if (clazz.equals(Float.class)) return ((T) (Float) num.value().floatValue());
            if (clazz.equals(Double.class)) return ((T) (Double) num.value().doubleValue());
        }
        if (value instanceof JSON5Boolean bool) {
            if (clazz.equals(Boolean.class)) return ((T) (Boolean) bool.value);
        }
        return clazz.cast(value);
    }
}
