package edu.montana.notch;

import edu.montana.notch.chisel.NotchParseErrors;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.TokenStream;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.type.LiteralTokenType;
import edu.montana.notch.console.NotchShell;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.builtins.NotchStructureFunction;
import edu.montana.notch.statements.NotchStatement;
import edu.montana.notch.templates.NotchTemplates;
import edu.montana.notch.types.coercions.Coercion;
import edu.montana.notch.util.BetterMap;

import java.util.LinkedHashMap;
import java.util.Map;

import static edu.montana.notch.chisel.type.BooleanTokenType.BOOL;
import static edu.montana.notch.chisel.type.IdentTokenType.IDENT;
import static edu.montana.notch.chisel.type.IntegerTokenType.INT;
import static edu.montana.notch.chisel.type.WhitespaceTokenType.WHITESPACE;
import static edu.montana.notch.token.NotchTokenTypeKeyword.NOTCH_KEYWORD;
import static edu.montana.notch.token.NotchStringTokenType.STR;
import static edu.montana.notch.token.TerseStringTokenType.TERSE_STRING;

public class Notch {
    private Notch() {
    }

    public static final Tokenizer TOKENIZER = new Tokenizer()
            .withTokenType("_ws", WHITESPACE)
            .withTokenType("string", STR)
            .withTokenType("string", TERSE_STRING)
            .withTokenType("bool", BOOL)
            .withTokenType("keyword", NOTCH_KEYWORD)
            .withTokenType("ident", IDENT)
            .withTokenType("int", INT)
            .withTokenTypes(LiteralTokenType.COMMON);

    public static <T> T eval(String code) {
        return (T) eval(code, Object.class);
    }

    public static <T> T eval(String code, Class<T> targetType) {
        TokenStream tokens = Notch.TOKENIZER.tokenize(new Source("<eval>", code));
        NotchParser notchParser = new NotchParser(tokens);
        NotchExpression notchExpression = notchParser.parseExpression();
        Object result = notchExpression.evaluate();
        if (!targetType.isInstance(result)) {
            Coercion coercer = Coercion.resolve(result.getClass(), targetType);
            if (coercer != null) {
                result = coercer.coerce(result);
            } else {
                throw new IllegalArgumentException("Result of type " + result.getClass() + " could not be coerced to " + targetType);
            }
        }
        return (T) result;
    }

    public static final NotchTemplates TEMPLATES = new NotchTemplates();

    public static String render(String path, Object... kv) {
        return TEMPLATES.renderTemplate(path, toMap(kv));
    }

    public static void render(String path, Appendable out, Object... kv) {
        TEMPLATES.renderTemplate(path, toMap(kv), new Drain(out));
    }

    public static void run(String program) {
        run(new Source("<embed>", program));
    }

    public static void run(Source source) {
        run(source, new NotchRuntime(source));
    }

    public static Object run(Source source, NotchRuntime runtime) {
        TokenStream tokens = TOKENIZER.tokenize(source);
        NotchParser parser = new NotchParser(tokens);
        NotchElement element = parser.parse();
        if (parser.hasErrors()) {
            throw new NotchParseErrors(parser.getDiagnostics());
        }
        if (element instanceof NotchStatement stmt) {
            runtime.execute(stmt);
            return null;
        } else if (element instanceof NotchExpression expr) {
            return runtime.evaluate(expr);
        }
        return null;
    }


    private static Map<String, Object> toMap(Object[] kv) {
        if (kv == null || kv.length == 0) return Map.of();
        if ((kv.length & 1) != 0) {
            throw new IllegalArgumentException("render() requires an even number of key/value arguments");
        }
        var map = new LinkedHashMap<String, Object>(kv.length / 2);
        for (int i = 0; i < kv.length; i += 2) {
            if (!(kv[i] instanceof String key)) {
                throw new IllegalArgumentException("render() key at index " + i + " must be a String, got " + (kv[i] == null ? "null" : kv[i].getClass().getName()));
            }
            map.put(key, kv[i + 1]);
        }
        return map;
    }

    public static void main(String[] args) {
        NotchShell.start(true);
    }

    public static Map<String, Object> defaultGlobals() {
        var out = new BetterMap<String, Object>();
        out.put("structure", NotchStructureFunction.getInstance());
        return out;
    }
}
