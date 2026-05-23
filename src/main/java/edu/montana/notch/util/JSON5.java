package edu.montana.notch.util;

import edu.montana.notch.chisel.*;
import edu.montana.notch.chisel.type.LiteralTokenType;
import edu.montana.notch.json5.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static edu.montana.notch.json5.JSON5TokenTypeComment.JSON5_COMMENT;
import static edu.montana.notch.json5.JSON5TokenTypeIdent.JSON5_IDENT;
import static edu.montana.notch.json5.JSON5TokenTypeNumber.JSON5_NUMBER;
import static edu.montana.notch.json5.JSON5TokenTypeString.JSON5_STRING;
import static edu.montana.notch.json5.JSON5TokenTypeWhitespace.JSON5_WHITESPACE;

public class JSON5 {
    public static final Tokenizer tokenizer = new Tokenizer()
            .withTokenType("_ws", JSON5_WHITESPACE)
            .withTokenType("_comment", JSON5_COMMENT)
            .withTokenTypes("{", new LiteralTokenType("{"))
            .withTokenTypes("}", new LiteralTokenType("}"))
            .withTokenTypes("[", new LiteralTokenType("["))
            .withTokenTypes("]", new LiteralTokenType("]"))
            .withTokenTypes(",", new LiteralTokenType(","))
            .withTokenTypes(":", new LiteralTokenType(":"))
            .withTokenType("num", JSON5_NUMBER)
            .withTokenType("ident", JSON5_IDENT)
            .withTokenType("string", JSON5_STRING);

    public static TokenStream tokenize(Source source) {
        return tokenizer.tokenize(source);
    }

    public static JSON5Object parseObject(Source source) {
        var parser = new JSON5Parser(source);
        return parser.parseObject();
    }

    public static JSON5Array parseArray(Source source) {
        var parser = new JSON5Parser(source);
        return parser.parseArray();
    }

    public static <T extends JSON5Value> T parse(Source source) {
        var parser = new JSON5Parser(source);
        var value = parser.parseValue();
        if (value == null) {
            final var diag = new Diagnostic();
            diag.highlight(parser.currentToken());
            diag.note("unable to parse value");
            throw new ParseException(diag);
        }
        return (T) value;
    }

    public static final JSON5Boolean TRUE = JSON5Boolean.TRUE;
    public static final JSON5Boolean FALSE = JSON5Boolean.FALSE;
    public static final JSON5Null NULL = JSON5Null.NULL;

    public static JSON5String str(Object value) {
        return new JSON5String("" + value);
    }

    public static JSON5Integer number(byte value) {
        return new JSON5Integer(value);
    }

    public static JSON5Integer number(char value) {
        return new JSON5Integer(value);
    }

    public static JSON5Integer number(short value) {
        return new JSON5Integer(value);
    }

    public static JSON5Integer number(int value) {
        return new JSON5Integer(value);
    }

    public static JSON5Integer number(long value) {
        return new JSON5Integer(value);
    }

    public static JSON5Decimal number(float value) {
        return new JSON5Decimal(value);
    }

    public static JSON5Decimal number(double value) {
        return new JSON5Decimal(value);
    }

    public static JSON5Value number(Number n) {
        Objects.requireNonNull(n);
        if (n instanceof Double || n instanceof Float || n instanceof BigDecimal) {
            return number(n.doubleValue());
        }
        return number(n.longValue());
    }

    public static JSON5Boolean bool(boolean b) {
        return b ? JSON5Boolean.TRUE : JSON5Boolean.FALSE;
    }

    public static JSON5Value valueOf(Object value) {
        if (value == null) return NULL;
        if (value instanceof JSON5Value v) return v;
        if (value instanceof Boolean b) return bool(b);
        if (value instanceof String s) return str(s);
        if (value instanceof Number n) return number(n);
        if (value instanceof Object[] arr) return new JSON5Array(arr);

        var clazz = value.getClass();
        Method toJsonMethod = null;
        try {
            toJsonMethod = clazz.getMethod("toJson");
        } catch (NoSuchMethodException ignored) {
        }
        if (toJsonMethod != null) {
            Object json;
            try {
                json = toJsonMethod.invoke(value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("failed to call 'toJson' on " + clazz.getName(), e);
            }
            return valueOf(json);
        }

        // people could override these & have containers that extend these
        if (value instanceof Iterable<?> iter) return new JSON5Array(iter);
        if (value instanceof Map<?, ?> m) return new JSON5Object(m);

        throw new RuntimeException("unable to json encode decimalValue " + clazz.getName() + ": " + value);
    }

    public static JSON5Array array(Object... values) {
        return new JSON5Array(values);
    }

    public static JSON5Array array(Iterable<?> values) {
        return new JSON5Array(values);
    }

    public static JSON5Object object(Object... keyValues) {
        assert keyValues.length % 2 == 0;
        var out = new JSON5Object();
        for (int i = 0; i < keyValues.length; i += 2) {
            final var key = ((String) keyValues[i]);
            out.put(key, keyValues[i + 1]);
        }
        return out;
    }

    public static JSON5Object object(Map<?, ?> values) {
        return new JSON5Object(values);
    }

    public interface UsesJson {
        default JSON5Object object(Object... keyValues) {
            return new JSON5Object(keyValues);
        }

        default JSON5Array array(Object... elements) {
            return new JSON5Array(elements);
        }
    }
}
